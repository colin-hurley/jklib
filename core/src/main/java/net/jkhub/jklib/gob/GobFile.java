package net.jkhub.jklib.gob;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public final class GobFile implements Closeable {

	private static final byte[] ZEROES = new byte[128]; // Elements are initialized to zero

	private final RandomAccessFile file;
	private final InputStream input;
	@SuppressWarnings("unused")
	private final OutputStream output;

	private GobHeader header;

	public GobFile(RandomAccessFile file) {
		Validate.notNull(file, "file must not be null");
		this.file = file;
		this.input = Channels.newInputStream(file.getChannel());
		this.output = Channels.newOutputStream(file.getChannel());
	}

	@Override
	public void close() throws IOException {
		this.file.close();
	}

	@SuppressWarnings("resource") // 'data' is just a wrapper, no actual resource leak
	public GobHeader getHeader() throws GobException, IOException {
		if (header == null) {
			file.seek(0);
			if (!"GOB ".equals(readText(input, 4))) {
				throw new GobException("Not a GOB file");
			}
			long entryOffset = EndianUtils.readSwappedUnsignedInteger(input) - 4;
			long entryCountOffset = EndianUtils.readSwappedUnsignedInteger(input);

			file.seek(entryCountOffset);
			int entryCount = EndianUtils.readSwappedInteger(input);

			file.seek(entryOffset);
			// We're about to read a large contiguous region, buffer it for performance
			InputStream buffered = bufferedInput();
			List<GobEntry> entries = new ArrayList<>(entryCount);
			for (int i = 0; i < entryCount; i++) {
				long offset = EndianUtils.readSwappedUnsignedInteger(buffered);
				long length = EndianUtils.readSwappedUnsignedInteger(buffered);
				String name = StringUtils.trimToNull(readText(buffered, 128));
				if (name == null) {
					throw new GobException("Entry name is blank");
				}
				if (offset < 0) {
					throw new GobException(String.format("Entry '%s' has invalid offset: %d", name, offset));
				}
				if (length <= 0) {
					throw new GobException(String.format("Entry '%s' has invalid length: %d", name, offset));
				}
				entries.add(new GobEntry(new GobPath(name), offset, length));
			}

			header = new GobHeader(entries);
		}
		return header;
	}

	public void setHeader(GobHeader header) throws GobException, IOException {
		file.setLength(calculateFileLength(header));
		file.seek(0);
		OutputStream buffered = bufferedOutput();
		writeText(buffered, "GOB ");
		EndianUtils.writeSwappedInteger(buffered, 0x14);
		EndianUtils.writeSwappedInteger(buffered, 0x0C);
		EndianUtils.writeSwappedInteger(buffered, header.getEntries().size());
		for (GobEntry entry : header.getEntries()) {
			EndianUtils.writeSwappedInteger(buffered, (int) entry.getOffset());
			EndianUtils.writeSwappedInteger(buffered, (int) entry.getLength());
			int wrote = writeText(buffered, entry.getPath().getPath());
			int padLength = 128 - wrote;
			if (padLength > 0) {
				buffered.write(ZEROES, 0, padLength);
			}
		}
		buffered.flush();
		this.header = header;
	}

	public InputStream readData(GobPath path) throws GobException, IOException {
		GobEntry entry = getHeader().getEntry(path);
		if (entry == null) {
			throw new IllegalArgumentException("Entry not found in GOB file: " + path);
		}

		file.seek(entry.getOffset());
		return new BoundedInputStream(bufferedInput(), entry.getLength());
	}

	public OutputStream writeData(GobPath path) throws GobException, IOException {
		GobEntry entry = getHeader().getEntry(path);
		if (entry == null) {
			throw new IllegalArgumentException("Entry not found in GOB file: " + path);
		}

		file.seek(entry.getOffset());
		// XXX: Return unbounded stream reference because Commons IO apparently doesn't
		// have a bounded output stream. For now, just trust that the caller won't
		// overflow the data entry
		return bufferedOutput();
	}

	private long calculateFileLength(GobHeader header) {
		long length = 0;
		for (GobEntry entry : header.getEntries()) {
			length = Math.max(length, entry.getLength() + entry.getOffset());
		}
		return length;
	}

	private String readText(InputStream input, int length) throws IOException {
		return new String(IOUtils.readFully(input, length), US_ASCII);
	}

	private int writeText(OutputStream output, String text) throws IOException {
		byte[] bytes = text.getBytes(US_ASCII);
		IOUtils.write(bytes, output);
		return bytes.length;
	}

	private InputStream bufferedInput() {
		return new BufferedInputStream(new CloseShieldInputStream(input));
	}

	private OutputStream bufferedOutput() {
		return new BufferedOutputStream(new CloseShieldOutputStream(output));
	}

}
