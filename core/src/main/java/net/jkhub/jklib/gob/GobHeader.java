package net.jkhub.jklib.gob;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public final class GobHeader {

	private final Map<GobPath, GobEntry> entries = new TreeMap<>();

	public GobHeader(Collection<GobEntry> entries) throws GobException {
		if (entries.isEmpty()) {
			throw new GobException("GOB file cannot be empty");
		}
		for (GobEntry entry : entries) {
			this.entries.put(entry.getPath(), entry);
		}
	}

	public GobEntry getEntry(GobPath path) {
		return entries.get(path);
	}

	public Collection<GobEntry> getEntries() {
		return Collections.unmodifiableCollection(entries.values());
	}

}
