package jawaitasync.processor;

import jawaitasync.Promise;
import jawaitasync.ResultRunnable;
import jawaitasync.vfs.SVfsFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * http://asm.ow2.org/asm40/javadoc/user/org/objectweb/asm/MethodVisitor.html
 */
public class AsmProcessor {
	static private boolean isAwaitMethodCall(AbstractInsnNode node) {
		if (!(node instanceof MethodInsnNode)) return false;
		MethodInsnNode methodNode = (MethodInsnNode) node;
		if (!methodNode.owner.equals("jawaitasync/Promise")) return false;
		if (!methodNode.name.equals("await")) return false;
		return true;
	}

	static private boolean isCompleteMethodCall(AbstractInsnNode node) {
		if (!(node instanceof MethodInsnNode)) return false;
		MethodInsnNode methodNode = (MethodInsnNode) node;
		if (!methodNode.owner.equals("jawaitasync/Promise")) return false;
		if (!methodNode.name.equals("complete")) return false;
		return true;
	}

	private boolean hasAwait(MethodNode method) {
		Linq<AbstractInsnNode> instructions = new Linq<AbstractInsnNode>(method.instructions.toArray());
		for (AbstractInsnNode node : instructions) {
			if (isAwaitMethodCall(node)) return true;
		}
		return false;
	}

	private int getMethodArgumentCountIncludingThis(MethodNode method) {
		boolean isStatic = ((method.access & ACC_STATIC) != 0);
		int argumentCount = 0;
		if (!isStatic) argumentCount++;
		if (method.parameters != null) argumentCount += method.parameters.size();
		return argumentCount;
	}

	private LocalVariableNode[] getLocalsByIndex(MethodNode method) {
		int maxIndex = 0;
		for (LocalVariableNode lv : (LocalVariableNode[])new Linq(method.localVariables).toArray(LocalVariableNode.class)) {
			maxIndex = Math.max(maxIndex, lv.index);
		}
		LocalVariableNode[] nodes = new LocalVariableNode[maxIndex + 1];
		for (LocalVariableNode lv : (LocalVariableNode[])new Linq(method.localVariables).toArray(LocalVariableNode.class)) {
			nodes[lv.index] = lv;
		}
		return nodes;
	}

	private ClassNode createTransformedClassForMethod(ClassNode classNode, MethodNode method) throws Exception {
		ClassNode cn = new ClassNode();
		cn.version = V1_8;
		cn.access = ACC_PUBLIC;
		cn.name = classNode.name + "$" + method.name + "$Runnable";
		//cn.name = classNode.name + "__" + method.name + "__Runnable";
		cn.superName = Type.getType(Object.class).getInternalName();
		//System.out.println("cn.superName: " + cn.superName);

		cn.interfaces.add(Type.getType(ResultRunnable.class).getInternalName());

		cn.fields.add(new FieldNode(ACC_PUBLIC, "state", "I", null, null));
		cn.fields.add(new FieldNode(ACC_PUBLIC, "promise", Type.getType(Promise.class).getDescriptor(), null, null));

		int argumentCount = getMethodArgumentCountIncludingThis(method);

		LocalVariableNode[] localsByIndex = getLocalsByIndex(method);

		for (LocalVariableNode lv : localsByIndex) {
			cn.fields.add(new FieldNode(ACC_PUBLIC, "local_" + lv.name, lv.desc, null, null));
			//System.out.println("local[]:" + lv.name + ", " + lv.desc + ", " + lv.index);
		}

		Type[] args = new Type[argumentCount];

		//System.out.println("argumentCount:" + argumentCount);

		for (int n = 0; n < argumentCount; n++) {
			LocalVariableNode lv2 = localsByIndex[n];
			args[n] = Type.getType(lv2.desc);
			//System.out.println("args[" + n + "]:" + lv2.name + ", " + lv2.desc);
		}

		// this, arguments from the function
		MethodNode mnc = new MethodNode(ACC_PUBLIC, "<init>", Type.getMethodType(Type.VOID_TYPE, args).getDescriptor(), null, null);
		mnc.instructions.add(new IntInsnNode(ALOAD, 0));
		mnc.instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));

		Type promiseType = Type.getType(Promise.class);

		mnc.instructions.add(new IntInsnNode(ALOAD, 0));
		mnc.instructions.add(new TypeInsnNode(NEW, promiseType.getInternalName()));
		mnc.instructions.add(new InsnNode(DUP));
		mnc.instructions.add(new MethodInsnNode(INVOKESPECIAL, promiseType.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false));
		mnc.instructions.add(new FieldInsnNode(PUTFIELD, cn.name, "promise", promiseType.getDescriptor()));

		mnc.instructions.add(new IntInsnNode(ALOAD, 0));
		mnc.instructions.add(new IntInsnNode(BIPUSH, 0));
		mnc.instructions.add(new FieldInsnNode(PUTFIELD, cn.name, "state", "I"));

		for (int n = 0; n < argumentCount; n++) {
			LocalVariableNode lv2 = localsByIndex[n];
			mnc.instructions.add(new IntInsnNode(ALOAD, 0));
			mnc.instructions.add(new IntInsnNode(ALOAD, n + 1));
			mnc.instructions.add(new FieldInsnNode(PUTFIELD, cn.name, "local_" + lv2.name, lv2.desc));
		}

		mnc.instructions.add(new InsnNode(RETURN));

		cn.methods.add(mnc);

		MethodNode mn = new MethodNode(ACC_PUBLIC, "run", Type.getMethodType(Type.VOID_TYPE, Type.getType(Object.class)).getDescriptor(), null, null);

		//mn.localVariables.add(0, new LocalVariableNode("this", cn.name, ));

		mn.instructions.add(method.instructions);

		// Convert iinc, into load and stores, so after we can convert locals into field access.
		for (AbstractInsnNode node : new Linq<AbstractInsnNode>(mn.instructions.toArray())) {
			if (node instanceof IincInsnNode) {
				IincInsnNode incNode = (IincInsnNode) node;
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ILOAD, incNode.var));
				list.add(new IntInsnNode(BIPUSH, incNode.incr));
				list.add(new InsnNode(IADD));
				list.add(new VarInsnNode(ISTORE, incNode.var));
				mn.instructions.insertBefore(node, list);
				mn.instructions.remove(node);
			}
		}

		List<LabelNode> stateLabelNodes = new ArrayList<>();
		LabelNode startLabel = new LabelNode();
		stateLabelNodes.add(startLabel);
		mn.instructions.insert(mn.instructions.getFirst(), startLabel);

		for (AbstractInsnNode node : new Linq<AbstractInsnNode>(mn.instructions.toArray())) {
			if (node instanceof VarInsnNode) {
				VarInsnNode varNode = (VarInsnNode) node;
				LocalVariableNode localVar = (LocalVariableNode) localsByIndex[varNode.var];
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0));
				//System.out.println(localVar.name);

				switch (varNode.getOpcode()) {
					case ILOAD:
					case LLOAD:
					case FLOAD:
					case DLOAD:
					case ALOAD:
						list.add(new FieldInsnNode(GETFIELD, cn.name, "local_" + localVar.name, localVar.desc));
						break;
					case ISTORE:
					case LSTORE:
					case FSTORE:
					case DSTORE:
					case ASTORE:
						list.add(new InsnNode(SWAP));
						list.add(new FieldInsnNode(PUTFIELD, cn.name, "local_" + localVar.name, localVar.desc));
						break;
					default:
						throw (new Exception("Can't handle opcode"));
				}

				mn.instructions.insertBefore(node, list);
				mn.instructions.remove(node);
				//System.out.println(varNode);
			}
			if (isAwaitMethodCall(node)) {
				//System.out.println("await!");
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getType(Promise.class).getInternalName(), "then", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ResultRunnable.class)), false));
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new IntInsnNode(BIPUSH, stateLabelNodes.size()));
				list.add(new FieldInsnNode(PUTFIELD, cn.name, "state", "I"));
				list.add(new InsnNode(RETURN));
				LabelNode awaitLabel = new LabelNode();
				stateLabelNodes.add(awaitLabel);
				list.add(awaitLabel);
				//list.add(new InsnNode(ACONST_NULL)); // Put the result of the promise here.
				list.add(new VarInsnNode(ALOAD, 1));

				mn.instructions.insertBefore(node, list);
				mn.instructions.remove(node);
			}

			if (isCompleteMethodCall(node)) {
				InsnList list = new InsnList();
				list.add(new IntInsnNode(ALOAD, 0));
				list.add(new FieldInsnNode(GETFIELD, cn.name, "promise", promiseType.getDescriptor()));
				list.add(new InsnNode(SWAP));
				list.add(new MethodInsnNode(INVOKEVIRTUAL, promiseType.getInternalName(), "resolve", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), false));

				mn.instructions.insertBefore(node, list);
				mn.instructions.remove(node);
			}

			switch (node.getOpcode()) {
				case ARETURN:
				case IRETURN:
				case LRETURN:
				case FRETURN:
				case DRETURN:
					mn.instructions.insertBefore(node, new InsnNode(RETURN));
					mn.instructions.remove(node);
					break;
			}
		}

		InsnList list = new InsnList();
		list.add(new VarInsnNode(ALOAD, 0));
		list.add(new FieldInsnNode(GETFIELD, cn.name, "state", "I"));
		//list.add(new TableSwitchInsnNode(0, stateLabelNodes.size() - 1, startLabel, stateLabelNodes.toArray(new LabelNode[stateLabelNodes.size()])));

		LabelNode[] labelNodes2 = (LabelNode[]) (new Linq(stateLabelNodes).toArray(LabelNode.class));
		//LabelNode startLabel = labelNodes2[0];
		list.add(new LookupSwitchInsnNode(startLabel, Linq.range(stateLabelNodes.size()), labelNodes2));
		mn.instructions.insert(mn.instructions.getFirst(), list);

		cn.methods.add(mn);

		return cn;
	}

	public boolean processFile(SVfsFile classFile) throws Exception {
		SVfsFile originalClassFile = classFile.getVfs().access(classFile.getName() + ".original");

		if ((classFile.lastModified() != originalClassFile.lastModified())) {
			//System.out.println("COPIED! " + originalClassFile.exists() + " " + classFile.lastModified() + " " + originalClassFile.lastModified());

			originalClassFile.write(classFile.read());
		}

		ClassNode clazz = getClassFromBytes(originalClassFile.read());
		ClassNode clazz2 = getClassFromBytes(originalClassFile.read());
		clazz.version = V1_8;

		int awaitMethodCount = 0;

		for (Object _method : clazz.methods) {
			MethodNode method = (MethodNode) _method;

			if (hasAwait(method)) {
				awaitMethodCount++;
				int argumentCount = getMethodArgumentCountIncludingThis(method);
				//System.out.println("argumentCount:" + argumentCount);

				//System.out.println("Method with await! " + method.name);

				method.instructions = new InsnList();

				MethodNode method2 = ClassNodeUtils.getMethod(clazz2, method.name, method.desc);

				//System.out.println(method2.name);
				ClassNode runClass = createTransformedClassForMethod(clazz2, method2);
				//System.out.println(outputFile.getParent());

				//System.out.println(runClass.name + ".class");
				classFile.getVfs().access(runClass.name + ".class").write(getClassBytes(runClass));
				method.instructions.add(new TypeInsnNode(NEW, runClass.name));
				method.instructions.add(new InsnNode(DUP));
				MethodNode mnInit = (MethodNode) runClass.methods.get(0);
				MethodNode mnRun = (MethodNode) runClass.methods.get(1);
				for (int n = 0; n < argumentCount; n++) {
					method.instructions.add(new IntInsnNode(ALOAD, n));
				}
				method.instructions.add(new MethodInsnNode(INVOKESPECIAL, runClass.name, mnInit.name, mnInit.desc, false));

				method.instructions.add(new InsnNode(DUP));
				method.instructions.add(new InsnNode(ACONST_NULL));
				method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, runClass.name, mnRun.name, mnRun.desc, false));

				if (Type.getType(method.desc).getReturnType() == Type.VOID_TYPE) {
					method.instructions.add(new InsnNode(RETURN));
				} else {
					method.instructions.add(new FieldInsnNode(GETFIELD, runClass.name, "promise", Type.getType(Promise.class).getDescriptor()));
					method.instructions.add(new InsnNode(ARETURN));
				}
			}
		}

		if (awaitMethodCount > 0) {
			classFile.write(getClassBytes(clazz));
			originalClassFile.setLastModified(classFile.lastModified());
			return true;
		} else {
			return false;
		}
	}

	private static ClassNode getClassFromBytes(byte[] data) throws Exception {
		ClassReader cr = new ClassReader(data);
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		//cn.accept(cr, 0);
		return cn;
	}

	private static byte[] getClassBytes(ClassNode cn) throws Exception {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cn.accept(cw);
		return cw.toByteArray();
	}
}

