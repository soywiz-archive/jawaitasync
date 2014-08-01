package jawaitasync.processor;

import com.sun.deploy.util.StringUtils;
import jawaitasync.Promise;
import jawaitasync.ResultRunnable;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

	private ClassNode createTransformedClassForMethod(ClassNode classNode, MethodNode method) throws Exception {
		ClassNode cn = new ClassNode();
		cn.version = V1_6;
		cn.access = ACC_PUBLIC;
		cn.name = classNode.name + "$" + method.name + "$Runnable";
		//cn.name = classNode.name + "__" + method.name + "__Runnable";
		cn.superName = Type.getType(Object.class).getInternalName();
		//System.out.println("cn.superName: " + cn.superName);

		cn.interfaces.add(Type.getType(ResultRunnable.class).getInternalName());

		cn.fields.add(new FieldNode(ACC_PUBLIC, "state", "I", null, null));
		cn.fields.add(new FieldNode(ACC_PUBLIC, "promise", Type.getType(Promise.class).getDescriptor(), null, null));

		int argumentCount = getMethodArgumentCountIncludingThis(method);

		for (LocalVariableNode lv : new Linq<LocalVariableNode>(method.localVariables)) {
			cn.fields.add(new FieldNode(ACC_PUBLIC, "local_" + lv.name, lv.desc, null, null));
		}

		Type[] args = new Type[argumentCount];

		for (int n = 0; n < argumentCount; n++) {
			LocalVariableNode lv2 = (LocalVariableNode)method.localVariables.get(n);
			args[n] = Type.getType(lv2.desc);
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
			LocalVariableNode lv2 = (LocalVariableNode)method.localVariables.get(n);
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
				IincInsnNode incNode = (IincInsnNode)node;
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
		//LabelNode startLabel = new LabelNode();
		//stateLabelNodes.add(startLabel);
		//mn.instructions.insertBefore(mn.instructions.getFirst(), startLabel);

		for (AbstractInsnNode node : new Linq<AbstractInsnNode>(mn.instructions.toArray())) {
			if (node instanceof VarInsnNode) {
				VarInsnNode varNode = (VarInsnNode) node;
				LocalVariableNode localVar = (LocalVariableNode) method.localVariables.get(varNode.var);
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0));
				System.out.println(localVar.name);

				switch (varNode.getOpcode()) {
					case ILOAD:
					case LLOAD:
					case FLOAD:
					case DLOAD:
					case ALOAD:
						list.add(new FieldInsnNode(GETFIELD, cn.name, "local_" + localVar.name, localVar. desc));
						break;
					case ISTORE:
					case LSTORE:
					case FSTORE:
					case DSTORE:
					case ASTORE:
						list.add(new FieldInsnNode(PUTFIELD, cn.name, "local_" + localVar.name, localVar.desc));
						break;
					default:
						throw(new Exception("Can't handle opcode"));
				}

				mn.instructions.insertBefore(node, list);
				mn.instructions.remove(node);
				//System.out.println(varNode);
			}
			if (isAwaitMethodCall(node)) {
				System.out.println("await!");
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getType(Promise.class).getInternalName(), "then", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ResultRunnable.class)), false));
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new IntInsnNode(BIPUSH, stateLabelNodes.size()));
				list.add(new FieldInsnNode(PUTFIELD, cn.name, "state", "I"));
				list.add(new InsnNode(RETURN));
				LabelNode awaitLabel = new LabelNode(); stateLabelNodes.add(awaitLabel);
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

		LabelNode[] labelNodes2 = (LabelNode[])(new Linq(stateLabelNodes).toArray(LabelNode.class));
		LabelNode startLabel = labelNodes2[0];
		list.add(new LookupSwitchInsnNode(startLabel, Linq.range(1, stateLabelNodes.size()), labelNodes2));
		mn.instructions.insert(mn.instructions.getFirst(), list);

		cn.methods.add(mn);

		return cn;
	}

	public void processFile(File inputFile, File outputFile) throws Exception {
		File originalInput = new File(inputFile.getAbsolutePath() + ".original");
		if ((inputFile.lastModified() != originalInput.lastModified())) {
			System.out.println("COPIED! " + originalInput.exists() + " " + inputFile.lastModified() + " " + originalInput.lastModified());
			FileUtils.copyFile(inputFile, originalInput);
		}

		ClassNode clazz = readClass(originalInput);
		ClassNode clazz2 = readClass(originalInput);
		clazz.version = V1_6;

		for (Object _method : clazz.methods) {
			MethodNode method = (MethodNode) _method;

			if (hasAwait(method)) {
				int argumentCount = getMethodArgumentCountIncludingThis(method);
				System.out.println("argumentCount:" + argumentCount);

				System.out.println("Method with await! " + method.name);

				LocalVariableNode[] localVariables = (LocalVariableNode[])method.localVariables.toArray(new LocalVariableNode[0]);
				System.out.println("localVariables:" + localVariables);

				if (true) {
					//clazz2
					MethodNode method2 = ClassNodeUtils.getMethod(clazz2, method.name, method.desc);

					ClassNode runClass = createTransformedClassForMethod(clazz2, method2);
					//System.out.println(outputFile.getParent());
					writeClass(new File(outputFile.getParent() + "/" + runClass.name + ".class"), runClass);
					method.instructions = new InsnList();
					method.instructions.add(new TypeInsnNode(NEW, runClass.name));
					method.instructions.add(new InsnNode(DUP));
					MethodNode mnInit = (MethodNode)runClass.methods.get(0);
					MethodNode mnRun = (MethodNode)runClass.methods.get(1);
					for (int n = 0; n < argumentCount; n++) method.instructions.add(new IntInsnNode(ALOAD, n));
					method.instructions.add(new MethodInsnNode(INVOKESPECIAL, runClass.name, mnInit.name, mnInit.desc, false));
					//method.instructions.add(new InsnNode(DUP));
					method.instructions.add(new IntInsnNode(ASTORE, 1));
					method.instructions.add(new IntInsnNode(ALOAD, 1));
					method.instructions.add(new InsnNode(ACONST_NULL));
					method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, runClass.name, mnRun.name, mnRun.desc, false));
					method.instructions.add(new IntInsnNode(ALOAD, 1));
					method.instructions.add(new FieldInsnNode(GETFIELD, runClass.name, "promise", Type.getType(Promise.class).getDescriptor()));
					method.instructions.add(new InsnNode(ARETURN));
				} else {
					method.instructions.add(new InsnNode(ACONST_NULL));
					method.instructions.add(new InsnNode(ARETURN));
				}
			}
		}
		//FileUtils.copyFile();
		writeClass(outputFile, clazz);

		originalInput.setLastModified(inputFile.lastModified());
	}

	public void test() throws Exception {
		//System.out.println("Working Directory = " + System.getProperty("user.dir") + "/../out/jawaitasync/PromiseExample.class");
		File outputFile = new File(System.getProperty("user.dir") + "/../out/PromiseExample.class");
		processFile(outputFile, outputFile);
		return;

//        ClassNode cn = new ClassNode();
//        cn.version = V1_8;
//        cn.access = ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE;
//        cn.name = "pkg/Comparable";
//        cn.superName = "java/lang/Object";
//        cn.interfaces.add("pkg/Mesurable");
//        cn.fields.add(new FieldNode(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "LESS", "I", null, new Integer(-1)));
//        cn.fields.add(new FieldNode(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "EQUAL", "I", null, new Integer(0)));
//        cn.fields.add(new FieldNode(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "GREATER", "I", null, new Integer(1)));
//        cn.methods.add(new MethodNode(ACC_PUBLIC + ACC_ABSTRACT, "compareTo", "(Ljava/lang/Object;)I", null, null));
//        MethodNode mn = new MethodNode(ACC_PUBLIC, "compareTo2", "(Ljava/lang/Object;)I", null, null);
//        InsnList il = new InsnList();
//        //il.add(new IntInsnNode(ILOAD_, 1));
//        il.add(new InsnNode(ICONST_1));
//        il.add(new InsnNode(IRETURN));
//        mn.instructions.insert(il);
//        cn.methods.add(mn);
//
//        //ClassLoader cl = new ClassLoader();
//        //new ClassReader()
//
//        writeClass(new File("c:/temp/out.class"), cn);
//        ClassNode cn2 = readClass(new File("c:/temp/out.class"));
//
//        MethodNode mn1 = new Linq<MethodNode>(cn2.methods).first(item -> (item.name.equals("compareTo2")));
//        //mn1.instructions = new InsnList();
//        mn1.instructions.remove(mn1.instructions.getFirst());
//        mn1.instructions.insertBefore(mn1.instructions.getLast(), new InsnNode(ICONST_2));
//        System.out.println(mn1.instructions.toString());
//
//        //Opcodes
//        //System.out.println(mn1.name);
//        //System.out.println(cn2.methods.stream().anyMatch((item) -> item.name == "compareTo2"));
//
//        writeClass(new File("c:/temp/out2.class"), cn2);
	}

	private static ClassNode readClass(File file) throws IOException {
		ClassReader cr = new ClassReader(FileUtils.readFileToByteArray(file));
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		//cn.accept(cr, 0);
		return cn;
	}

	private static void writeClass(File file, ClassNode cn) throws IOException {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		cn.accept(cw);
		FileUtils.writeByteArrayToFile(file, cw.toByteArray());
	}

	public static void main(String[] args) throws Exception {
		new AsmProcessor().test();
	}
}

