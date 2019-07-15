package net.jkhub.jklib.gob;

import java.io.File;
import java.util.Comparator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public final class GobPath implements Comparable<GobPath> {

	private Comparator<GobPath> ORDER = Comparator.nullsFirst(Comparator.comparing(GobPath::toFile));

	private final String path;
	private final File file;

	public GobPath(String path) throws GobException {
		if (StringUtils.isBlank(path)) {
			throw new GobException("path must not be blank");
		}
		String p = FilenameUtils.separatorsToWindows(FilenameUtils.normalize(path));
		if (p == null) {
			throw new GobException("path is invalid: " + path);
		}
		this.path = p;
		this.file = new File(p);
		if (this.path.length() > 128) {
			throw new GobException("path exceeds 128 characters: " + path);
		}
	}

	public String getPath() {
		return path;
	}

	public File toFile() {
		return file;
	}

	@Override
	public int compareTo(GobPath o) {
		return ORDER.compare(this, o);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GobPath other = (GobPath) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.path;
	}

}
