package jawaitasync.vfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SVfsFile {
	private SVfs vfs;
	private String name;

	public SVfsFile(SVfs vfs, String name) {
		this.vfs = vfs;
		this.name = name;
	}

	public SVfs getVfs() {
		return vfs;
	}

	public String getName() {
		return name;
	}

	public byte[] read() throws Exception {
		if (!this.vfs.has(this.name)) throw(new FileNotFoundException("Can't find file " + name + " @ " + vfs));
		return this.vfs.get(this.name);
	}

	public void write(byte[] data) throws Exception {
		this.vfs.put(this.name, data);
	}

	public boolean exists() {
		return this.vfs.has(this.name);
	}

	public String getParent() {
		String parent = new File(this.name).getParent();
		return (parent != null) ? parent : "/";
	}

	public long lastModified() { return vfs.lastModified(this.name); }
	public void setLastModified(long value) {
		vfs.setLastModified(this.name, value);
	}
}
