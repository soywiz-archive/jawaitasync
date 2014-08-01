package jawaitasync.processor;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.naming.NameNotFoundException;

public class ClassNodeUtils {
	static public MethodNode getMethod(ClassNode cn, String name) throws Exception {
		for (MethodNode mn : (MethodNode[])cn.methods.toArray(new MethodNode[0])) if (mn.name.equals(name)) return mn;
		throw(new NameNotFoundException("Can't find method " + name));
	}

	static public MethodNode getMethod(ClassNode cn, String name, String desc) throws Exception {
		for (MethodNode mn : (MethodNode[])cn.methods.toArray(new MethodNode[0])) if (mn.name.equals(name) && mn.desc.equals(desc)) return mn;
		throw(new NameNotFoundException("Can't find method " + name + " | " + desc));
	}
}

