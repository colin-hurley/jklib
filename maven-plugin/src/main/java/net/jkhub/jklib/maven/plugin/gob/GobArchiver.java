package net.jkhub.jklib.maven.plugin.gob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.ArchiveEntry;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.archiver.util.ResourceUtils;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;

import net.jkhub.jklib.gob.GobEntry;
import net.jkhub.jklib.gob.GobFile;
import net.jkhub.jklib.gob.GobHeader;
import net.jkhub.jklib.gob.GobPath;

public class GobArchiver extends AbstractArchiver {

	@Override
	protected String getArchiveType() {
		return "GOB";
	}

	@Override
	protected void close() throws IOException {
		// Nothing to do
	}

	@Override
	protected void execute() throws ArchiverException, IOException {
		ResourceIterator iter = getResources();

		File gobFile = getDestFile();

		if (gobFile == null) {
			throw new ArchiverException("You must set the destination gob file.");
		}
		if (gobFile.exists() && !gobFile.isFile()) {
			throw new ArchiverException(gobFile + " isn't a file.");
		}
		if (gobFile.exists() && !gobFile.canWrite()) {
			throw new ArchiverException(gobFile + " is read-only.");
		}

		getLogger().info("Building gob: " + gobFile.getAbsolutePath());

		Map<GobPath, ArchiveEntry> sources = new TreeMap<>();
		while (iter.hasNext()) {
			ArchiveEntry source = iter.next();

			if (source.getType() != ArchiveEntry.FILE) {
				getLogger().debug("Ignoring non-file resource: " + source.getName());
				continue;
			}

			PlexusIoResource resource = source.getResource();
			if (ResourceUtils.isSame(resource, gobFile)) {
				throw new ArchiverException("A gob file cannot include itself.");
			}

			sources.put(new GobPath(source.getName()), source);
		}

		Map<GobPath, GobEntry> entries = new TreeMap<>();
		long offset = 0x10 + (sources.size() * 136);
		for (Entry<GobPath, ArchiveEntry> pathEntry : sources.entrySet()) {
			GobPath path = pathEntry.getKey();
			ArchiveEntry source = pathEntry.getValue();
			PlexusIoResource resource = source.getResource();

			long size = resource.getSize();
			if (size == PlexusIoResource.UNKNOWN_RESOURCE_SIZE) {
				getLogger().warn("Ignoring resource of unknown size: " + source.getName());
				continue;
			}

			GobEntry entry = new GobEntry(path, offset, size);
			entries.put(path, entry);

			if (getLogger().isDebugEnabled()) {
				getLogger().debug("Adding " + entry);
			}

			offset += size;
		}

		GobHeader header = new GobHeader(entries.values());

		try (GobFile gob = new GobFile(new RandomAccessFile(gobFile, "rw"))) {
			gob.setHeader(header);
			for (GobEntry entry : header.getEntries()) {
				InputStream input = IOUtils.buffer(sources.get(entry.getPath()).getInputStream());
				OutputStream output = IOUtils.buffer(gob.writeData(entry.getPath()));
				IOUtils.copy(input, output);
				output.flush();
			}
		} catch (IOException e) {
			throw new ArchiverException(e.getMessage(), e);
		}
	}

}
