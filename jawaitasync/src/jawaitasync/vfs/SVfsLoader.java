package jawaitasync.vfs;

public class SVfsLoader extends ClassLoader {
	private SVfs vfs;

	public SVfsLoader(SVfs vfs, ClassLoader parent) {
		super(parent);
		this.vfs = vfs;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			SVfsFile classFile = vfs.access(name + ".class");
			//System.out.println("classFile:" + classFile.getName());
			if (classFile.exists()) {
				byte[] originalClass = new byte[0];
				originalClass = classFile.read();
				return this.defineClass(name, originalClass, 0, originalClass.length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.loadClass(name, resolve);
	}
}
