package net.jkhub.jklib.maven.plugin.gob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.archiver.AbstractUnArchiver;
import org.codehaus.plexus.archiver.ArchiverException;

import net.jkhub.jklib.gob.GobEntry;
import net.jkhub.jklib.gob.GobFile;

public class GobUnArchiver extends AbstractUnArchiver {

	@Override
	protected void execute() throws ArchiverException {
		execute(getSourceFile(), getDestDirectory());
	}

	@Override
	protected void execute(String path, File outputDirectory) throws ArchiverException {
		execute(new File(path), getDestDirectory());
	}

	protected void execute(File archive, File outputDirectory) throws ArchiverException {
		try (GobFile gob = new GobFile(new RandomAccessFile(archive, "r"))) {
			for (GobEntry entry : gob.getHeader().getEntries()) {
				File file = new File(outputDirectory, entry.getPath().getPath());
				InputStream input = gob.readData(entry.getPath());
				try (FileOutputStream output = FileUtils.openOutputStream(file)) {
					IOUtils.copy(input, output);
				}
			}
		} catch (IOException e) {
			throw new ArchiverException(e.getMessage(), e);
		}
	}

}
