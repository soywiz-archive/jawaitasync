package jawaitasync.vfs;

import org.apache.commons.io.FileUtils;

import java.io.File;
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
		return FileUtils.readFileToByteArray(getFile(name));
	}

	@Override
	protected Boolean _has(String name) {
		return getFile(name).exists();
	}

	@Override
	public void _put(String name, byte[] data) throws IOException {
		FileUtils.writeByteArrayToFile(getFile(name), data);
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
