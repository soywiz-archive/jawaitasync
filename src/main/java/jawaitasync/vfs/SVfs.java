package jawaitasync.vfs;

import java.io.IOException;

abstract public class SVfs {

	private String normalizeName(String name) {
		while ((name.length() > 0) && name.startsWith("/")) name = name.substring(1);
		return name;
	}

	final public Boolean has(String name) { return _has(normalizeName(name)); }
	final public byte[] get(String name) throws Exception { return this._get(normalizeName(name)); }
	final public void put(String name, byte[] data) throws Exception { this._put(normalizeName(name), data);}
	final public long lastModified(String name) {
		return this._lastModified(normalizeName(name));
	}
	final public void setLastModified(String name, long value) {
		this._setLastModified(normalizeName(name), value);
	}
	final public SVfsFile access(String name) {
		return new SVfsFile(this, name);
	}

	abstract protected byte[] _get(String name) throws Exception;
	abstract protected void _put(String name, byte[] data) throws Exception;
	protected Boolean _has(String name) {
		try {
			get(name);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	protected long _lastModified(String name) {
		return 0;
	}
	protected void _setLastModified(String name, long value) {

	}
}
