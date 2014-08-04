package jawaitasync.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileSVfs extends SVfs {
	private String path;

	public FileSVfs(String path) {
		this.path = path;
	}

	private String getPath(String name) {
		return this.path + "/" + name;
	}
	private File getFile(String name) {
		return new File(getPath(name));
	}

	@Override
	protected byte[] _get(String name) throws IOException {
		File file = getFile(name);
		byte[] out = null;
		try (FileInputStream fis = new FileInputStream(file)) {
			out = new byte[fis.available()];
			fis.read(out);
		}
		return out;
	}

	@Override
	protected Boolean _has(String name) {
		return getFile(name).exists();
	}

	@Override
	public void _put(String name, byte[] data) throws IOException {
		File file = getFile(name);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(data);
		}
	}

	@Override
	public long _lastModified(String name) {
		return getFile(name).lastModified();
	}

	@Override
	public void _setLastModified(String name, long value) {
		getFile(name).setLastModified(value);
	}

	@Override
	public String toString() {
		return "FileSVfs(" + path + ")";
	}
}
