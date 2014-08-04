package jawaitasync.processor;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.naming.NameNotFoundException;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.LRETURN;

public class ClassNodeUtils {
	static public MethodNode getMethod(ClassNode cn, String name) throws Exception {
		for (MethodNode mn : (MethodNode[])cn.methods.toArray(new MethodNode[0])) if (mn.name.equals(name)) return mn;
		throw(new NameNotFoundException("Can't find method " + name));
	}

	static public MethodNode getMethod(ClassNode cn, String name, String desc) throws Exception {
		for (MethodNode mn : (MethodNode[])cn.methods.toArray(new MethodNode[0])) if (mn.name.equals(name) && mn.desc.equals(desc)) return mn;
		throw(new NameNotFoundException("Can't find method " + name + " | " + desc));
	}

	static public FieldNode getField(ClassNode classNode, String name) {
		for (Object node : classNode.fields) {
			FieldNode fieldNode = (FieldNode) node;
			if (fieldNode.name.equals(name)) return fieldNode;
		}
		return null;
	}

	static public AbstractInsnNode getReturn(Type type) {
		if (type == Type.VOID_TYPE) return new InsnNode(RETURN);
		if (type == Type.INT_TYPE) return new InsnNode(IRETURN);
		if (type == Type.FLOAT_TYPE) return new InsnNode(FRETURN);
		if (type == Type.DOUBLE_TYPE) return new InsnNode(DRETURN);
		if (type == Type.LONG_TYPE) return new InsnNode(LRETURN);
		return new InsnNode(ARETURN);
	}
}

