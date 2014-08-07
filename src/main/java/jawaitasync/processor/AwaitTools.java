package jawaitasync.processor;

import jawaitasync.Promise;
import jawaitasync.vfs.FileSVfs;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.UnsupportedEncodingException;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class AwaitTools {
	static final boolean DEBUG = false;
	//static final boolean DEBUG = true;

	static public boolean isAwaitMethodCall(AbstractInsnNode node) {
		if (!(node instanceof MethodInsnNode)) return false;
		MethodInsnNode methodNode = (MethodInsnNode) node;
		if (!methodNode.owner.equals("jawaitasync/Promise")) return false;
		if (!methodNode.name.equals("await")) return false;
		return true;
	}

	static public boolean isCompleteMethodCall(AbstractInsnNode node) {
		if (!(node instanceof MethodInsnNode)) return false;
		MethodInsnNode methodNode = (MethodInsnNode) node;
		if (!methodNode.owner.equals("jawaitasync/Promise")) return false;
		if (!methodNode.name.equals("complete")) return false;
		return true;
	}

	static public boolean hasAwait(MethodNode method) {
		Linq<AbstractInsnNode> instructions = new Linq<AbstractInsnNode>(method.instructions.toArray());
		for (AbstractInsnNode node : instructions) {
			if (isAwaitMethodCall(node)) return true;
		}
		return false;
	}

	static public int getMethodArgumentCountIncludingThis(MethodNode method) {
		return Type.getMethodType(method.desc).getArgumentTypes().length + (((method.access & ACC_STATIC) != 0) ? 0 : 1);
	}

	static public LocalVariableNode[] getLocalsByIndex(MethodNode method) {
		int maxIndex = 0;
		for (LocalVariableNode lv : (LocalVariableNode[]) new Linq(method.localVariables).toArray(LocalVariableNode.class)) {
			//System.out.println(lv.name + ":" + lv.index);
			maxIndex = Math.max(maxIndex, lv.index);
		}
		LocalVariableNode[] nodes = new LocalVariableNode[maxIndex + 1];
		for (LocalVariableNode lv : (LocalVariableNode[]) new Linq(method.localVariables).toArray(LocalVariableNode.class)) {
			nodes[lv.index] = lv;
		}
		return nodes;
	}

	static public boolean classReferencesPromises(byte[] originalClassBytes) throws UnsupportedEncodingException {
		return Bytes.contains(originalClassBytes, Promise.class.getName().replace('.', '/').getBytes("UTF-8"));
	}

	static public void writeOriginalClass(ClassNode clazz, byte[] content) throws Exception {
		if (DEBUG) new FileSVfs("c:/temp").access(clazz.name.replace('/', '.') + ".original.class").write(content);
	}

	static public byte[] getClassBytes(ClassNode cn) throws Exception {
		try {
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			cn.accept(cw);

			if (DEBUG) new FileSVfs("c:/temp").access(cn.name.replace('/', '.') + ".debug.class").write(cw.toByteArray());

			return cw.toByteArray();
		} catch (Exception exception) {
			exception.printStackTrace();
			ClassWriter cw = new ClassWriter(0);
			cn.accept(cw);
			try {
				if (DEBUG) new FileSVfs("c:/temp").access(cn.name.replace('/', '.') + ".debug.class").write(cw.toByteArray());
			} catch (Throwable t) {

			}
			throw (exception);
		}
	}
}
