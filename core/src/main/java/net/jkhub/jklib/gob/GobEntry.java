package net.jkhub.jklib.gob;

public class GobEntry {

	private GobPath path;
	private long offset;
	private long length;

	public GobEntry(GobPath path, long offset, long length) {
		this.path = path;
		this.offset = offset;
		this.length = length;
	}

	public GobPath getPath() {
		return path;
	}

	public long getOffset() {
		return offset;
	}

	public long getLength() {
		return length;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		GobEntry other = (GobEntry) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GobEntry [path=" + path + ", offset=" + offset + ", length=" + length + "]";
	}

}
