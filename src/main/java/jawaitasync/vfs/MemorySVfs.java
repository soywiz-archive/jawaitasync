package jawaitasync.vfs;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MemorySVfs extends SVfs {
	Map<String, Entry> maps = new HashMap<>();

	@Override
	protected void _put(String name, byte[] data) {
		maps.put(name, new Entry(name, data, new Date().getTime()));
	}

	@Override
	protected Boolean _has(String name) {
		return maps.containsKey(name);
	}

	@Override
	protected byte[] _get(String name) {
		return maps.get(name).content;
	}

	@Override
	protected long _lastModified(String name) {
		if (!maps.containsKey(name)) return 0;
		return maps.get(name).lastModified;
	}

	@Override
	protected void _setLastModified(String name, long value) {
		if (!maps.containsKey(name)) return;
		maps.get(name).lastModified = value;
	}
}

class Entry {
	public String name;
	public byte[] content;
	public long lastModified;

	Entry(String name, byte[] content, long lastModified) {
		this.name = name;
		this.content = content;
		this.lastModified = lastModified;
	}
}