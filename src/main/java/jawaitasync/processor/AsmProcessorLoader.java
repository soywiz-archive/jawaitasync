package jawaitasync.processor;

import jawaitasync.InputStreamUtils;
import jawaitasync.vfs.MemorySVfs;
import jawaitasync.vfs.SVfs;
import jawaitasync.vfs.SVfsFile;

public class AsmProcessorLoader extends ClassLoader {
	private SVfs vfs = new MemorySVfs();

	public AsmProcessorLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			String classFileName = "/" + name.replace('.', '/') + ".class";
			SVfsFile classFile = vfs.access(classFileName);
			if (!classFile.exists()) {
				Class<?> newClass = super.loadClass(name, resolve);
				byte[] data = InputStreamUtils.load(newClass.getResourceAsStream(classFileName));
				if (data == null) return newClass;
				classFile.write(data);
				if (!(new AsmProcessor().processFile(classFile))) return newClass;
			}
			byte[] classData = classFile.read();
			return this.defineClass(name, classData, 0, classData.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw(new ClassNotFoundException("Can't find class " + name));
	}
}
