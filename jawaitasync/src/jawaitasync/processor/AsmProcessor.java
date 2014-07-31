package jawaitasync.processor;

import com.sun.deploy.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.objectweb.asm.Opcodes.*;

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

	private ClassNode createTransformedClassForMethod(ClassNode classNode, MethodNode method) throws Exception {
		ClassNode cn = new ClassNode();
		cn.version = V1_8;
		cn.access = ACC_PUBLIC;
		cn.name = classNode.name + "$" + method.name + "$Runnable";
		cn.superName = "java/lang/Object";
		cn.interfaces.add("java/lang/Runnable");

		cn.fields.add(new FieldNode(ACC_PUBLIC, "state", "I", null, new Integer(0)));
		for (LocalVariableNode lv : new Linq<LocalVariableNode>(method.localVariables)) {
			cn.fields.add(new FieldNode(ACC_PUBLIC, "local_" + lv.name, lv.desc, null, null));
		}

		MethodNode mn = new MethodNode(ACC_PUBLIC, "run", "()V", null, null);

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
		LabelNode startLabel = new LabelNode();
		stateLabelNodes.add(startLabel);
		mn.instructions.insertBefore(mn.instructions.getFirst(), startLabel);

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

				InsnList list = new InsnList();
				list.add(new InsnNode(POP));
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new IntInsnNode(BIPUSH, stateLabelNodes.size()));
				list.add(new FieldInsnNode(PUTFIELD, cn.name, "state", "I"));
				list.add(new InsnNode(RETURN));
				LabelNode awaitLabel = new LabelNode(); stateLabelNodes.add(awaitLabel);
				list.add(awaitLabel);
				list.add(new InsnNode(ACONST_NULL)); // Put the result of the promise here.

				mn.instructions.insertBefore(node, list);
				mn.instructions.remove(node);
				System.out.println("await!");
			}
			if (isCompleteMethodCall(node)) {
				mn.instructions.insertBefore(node, new InsnNode(POP));
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
		list.add(new TableSwitchInsnNode(0, stateLabelNodes.size() - 1, startLabel, stateLabelNodes.toArray(new LabelNode[stateLabelNodes.size()])));
		//list.add(new LookupSwitchInsnNode(startLabel, Linq.range(stateLabelNodes.size()), stateLabelNodes.toArray(new LabelNode[stateLabelNodes.size()])));
		mn.instructions.insertBefore(mn.instructions.getFirst(), list);

		cn.methods.add(mn);

		return cn;
	}

	public void processFile(File file) throws Exception {
		ClassNode clazz = readClass(file);
		for (Object _method : clazz.methods) {
			MethodNode method = (MethodNode) _method;

			if (hasAwait(method)) {
				System.out.println("Method with await! " + method.name);
				ClassNode runClass = createTransformedClassForMethod(clazz, method);
				writeClass(new File("c:/temp/" + runClass.name + ".class"), runClass);
				//System.out.println(method.desc);

			}
		}
		writeClass(new File("c:/temp/" + clazz.name + ".class"), clazz);
	}

	public void test() throws Exception {
		//System.out.println("Working Directory = " + System.getProperty("user.dir") + "/../out/jawaitasync/PromiseExample.class");
		processFile(new File(System.getProperty("user.dir") + "/../out/PromiseExample.class"));
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
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		FileUtils.writeByteArrayToFile(file, cw.toByteArray());
	}

	public static void main(String[] args) throws Exception {
		new AsmProcessor().test();
	}
}

interface Type {
}

class TypeVoid implements Type {
	@Override
	public String toString() {
		return "V";
	}
}

class TypeInteger implements Type {
	@Override
	public String toString() {
		return "I";
	}
}

class TypeClass implements Type {
	private Class clazz;

	TypeClass(Class clazz) {
		this.clazz = clazz;
	}

	@Override
	public String toString() {
		return "T" + this.clazz.getName().replace('.', '/') + ";";
	}
}

class TypeMethod implements Type {
	private Type[] argumentTypes;
	private Type returnType;

	TypeMethod(Type[] argumentTypes, Type returnType) {
		this.argumentTypes = argumentTypes;
		this.returnType = returnType;
	}

	@Override
	public String toString() {
		return "(" + StringUtils.join(Arrays.asList(argumentTypes), "") + ")" + this.returnType;
	}
}

class Linq<T> implements Iterable<T> {
	private Iterable<T> list;

	Linq(Iterable<T> list) {
		this.list = list;
	}

	Linq(T[] items) {
		this.list = Arrays.asList(items);
	}

	public T first(Predicate<T> predicate) {
		for (T item : list) {
			if (predicate.test(item)) return item;
		}
		return null;
	}

	static public int[] range(int count) {
		int[] result = new int[count];
		for (int n = 0; n < count; n++) result[n] = n;
		return result;
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		list.forEach(action);
	}

	@Override
	public Spliterator<T> spliterator() {
		return list.spliterator();
	}
}