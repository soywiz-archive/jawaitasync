package jawaitasync.processor;

import jawaitasync.Promise;
import jawaitasync.ResultRunnable;
import jawaitasync.processor.analyzer.TypeInterpreter;
import jawaitasync.processor.analyzer.TypeValue;
import jawaitasync.vfs.FileSVfs;
import jawaitasync.vfs.SVfsFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.*;

import static jawaitasync.processor.ClassNodeUtils.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * http://asm.ow2.org/asm40/javadoc/user/org/objectweb/asm/MethodVisitor.html
 */
public class AwaitProcessor {

	static final Type Promise_TYPE = Type.getType(Promise.class);
	static final Type Object_TYPE = Type.getType(Object.class);
	static final Type Long_TYPE = Type.getType(Long.class);
	static final Type Double_TYPE = Type.getType(Double.class);

	private MethodNode createTransformedConstructor(ClassNode cn, MethodNode method) throws Exception {
		int argumentCount = AwaitTools.getMethodArgumentCountIncludingThis(method);
		LocalVariableNode[] localsByIndex = AwaitTools.getLocalsByIndex(method);

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


		mnc.instructions.add(new IntInsnNode(ALOAD, 0));
		mnc.instructions.add(new TypeInsnNode(NEW, Promise_TYPE.getInternalName()));
		mnc.instructions.add(new InsnNode(DUP));
		mnc.instructions.add(new MethodInsnNode(INVOKESPECIAL, Promise_TYPE.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false));
		mnc.instructions.add(new FieldInsnNode(PUTFIELD, cn.name, "promise", Promise_TYPE.getDescriptor()));

		mnc.instructions.add(new IntInsnNode(ALOAD, 0));
		mnc.instructions.add(new IntInsnNode(BIPUSH, 0));
		mnc.instructions.add(new FieldInsnNode(PUTFIELD, cn.name, "state", "I"));

		for (int n = 0; n < argumentCount; n++) {
			LocalVariableNode lv2 = localsByIndex[n];
			Type lv2Type = Type.getType(lv2.desc);
			mnc.instructions.add(new IntInsnNode(ALOAD, 0));
			//System.out.println("Argument:" + lv2Type + ";" + ClassNodeUtils.getOperandType(lv2Type));
			mnc.instructions.add(getLoad(lv2Type, n + 1));
			mnc.instructions.add(new FieldInsnNode(PUTFIELD, cn.name, "local_" + lv2.name, lv2.desc));
		}

		mnc.instructions.add(new InsnNode(RETURN));
		return mnc;
	}

	static private MethodNode getMethod(ClassNode classNode, String name) {
		for (Object node : classNode.methods) {
			MethodNode methodNode = (MethodNode) node;
			if (methodNode.name.equals(name)) return methodNode;
		}
		return null;
	}

	private MethodNode getOrCreateMethodAccessMethod(ClassNode clazz, MethodNode originalMethodNode) throws Exception {
		String createdMethodName = originalMethodNode.name + "$Async$methodAccess";

		MethodNode createdMethod = ClassNodeUtils.getMethod(clazz, createdMethodName);

		if (createdMethod == null) {
			Type originalMethodType = Type.getMethodType(originalMethodNode.desc);
			Type clazzType = ClassNodeUtils.getType(clazz);
			List<Type> arguments = new LinkedList<>();
			boolean isStaticOriginal = ClassNodeUtils.isStatic(originalMethodNode);
			if (!isStaticOriginal) arguments.add(clazzType);
			for (Type argument : originalMethodType.getArgumentTypes()) arguments.add(argument);
			Type createdMethodType = Type.getMethodType(originalMethodType.getReturnType(), arguments.toArray(new Type[0]));
			Type[] createdArguments = createdMethodType.getArgumentTypes();
			createdMethod = new MethodNode(ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC, createdMethodName, createdMethodType.getDescriptor(), null, null);

			int invokeOpcode = isStaticOriginal ? INVOKESTATIC : INVOKEVIRTUAL;
			for (int n = 0; n < createdArguments.length; n++) {
				createdMethod.instructions.add(getLoad(createdArguments[n], n));
			}
			createdMethod.instructions.add(new MethodInsnNode(invokeOpcode, clazz.name, originalMethodNode.name, originalMethodNode.desc, false));
			createdMethod.instructions.add(getReturn(createdMethodType.getReturnType()));
			clazz.methods.add(createdMethod);
		}
		return createdMethod;
	}

	private MethodNode getOrCreateFieldAccessMethod(ClassNode outerClass, String fieldName, boolean write) {
		String methodName = fieldName + "$Async$" + (write ? "set" : "get");
		Type outerClassType = Type.getType("L" + outerClass.name + ";");
		MethodNode methodNode = getMethod(outerClass, methodName);

		if (methodNode == null) {
			FieldNode field = getField(outerClass, fieldName);
			Type fieldType = Type.getType(field.desc);
			boolean isStatic = (field.access & ACC_STATIC) != 0;
			List<Type> args = new LinkedList<>();
			if (!isStatic) args.add(outerClassType);
			if (write) args.add(fieldType);
			Type methodType = Type.getMethodType(write ? Type.VOID_TYPE : fieldType, args.toArray(new Type[1]));
			String methodTypeDesc = methodType.getDescriptor();
			//System.out.println(methodTypeDesc);
			methodNode = new MethodNode(ACC_PUBLIC | ACC_STATIC, methodName, methodTypeDesc, null, null);

			int opcode = write ? (isStatic ? PUTSTATIC : PUTFIELD) : (isStatic ? GETSTATIC : GETFIELD);
			int argn = 0;
			if (!isStatic) {
				methodNode.instructions.add(getLoad(args.get(argn), argn));
				argn++;
			}
			if (write) {
				methodNode.instructions.add(getLoad(args.get(argn), argn));
				argn++;
			}
			methodNode.instructions.add(new FieldInsnNode(opcode, outerClass.name, field.name, field.desc));
			methodNode.instructions.add(write ? getReturn(Type.VOID_TYPE) : getReturn(Type.getType(field.desc)));
			outerClass.methods.add(methodNode);
		}

		return methodNode;
	}

	private ClassNode createTransformedClassForMethod(ClassNode outerClass, ClassNode outerClassModify, MethodNode method) throws Exception {
		AwaitAnalyzer awaitAnalyzer = new AwaitAnalyzer(outerClass, method);

		int incrementalNameIndex = 0;

		ClassNode cn = new ClassNode();
		Type classType = Type.getType("L" + cn.name + ";");
		cn.version = outerClass.version;
		cn.access = ACC_SYNTHETIC | ACC_PRIVATE;
		cn.name = outerClass.name + "$" + method.name + "$Runnable";
		//cn.name = classNode.name + "$0";
		cn.sourceFile = outerClass.sourceFile;
		cn.outerClass = outerClass.name;
		cn.outerMethod = method.name;
		cn.outerMethodDesc = method.desc;

		Type methodReturnType = Type.getMethodType(method.desc).getReturnType();
		if ((methodReturnType != Type.VOID_TYPE) && (!methodReturnType.getDescriptor().equals(Type.getType(Promise.class).getDescriptor()))) {
			throw (new Exception("Method " + outerClass.name + ":" + method.name + " doesn't return a Promise or void"));
		}

		//cn.name = classNode.name + "__" + method.name + "__Runnable";
		cn.superName = Object_TYPE.getInternalName();
		//System.out.println("cn.superName: " + cn.superName);

		cn.interfaces.add(Type.getType(ResultRunnable.class).getInternalName());

		cn.fields.add(new FieldNode(ACC_PUBLIC, "state", "I", null, null));
		cn.fields.add(new FieldNode(ACC_PUBLIC, "promise", Promise_TYPE.getDescriptor(), null, null));

		int argumentCount = AwaitTools.getMethodArgumentCountIncludingThis(method);
		LocalVariableNode[] localsByIndex = AwaitTools.getLocalsByIndex(method);

		for (LocalVariableNode lv : (LocalVariableNode[])method.localVariables.toArray(new LocalVariableNode[0])) {
			String localName = "local_" + lv.name;
			if (ClassNodeUtils.getField(cn, localName) == null) {
				cn.fields.add(new FieldNode(ACC_PUBLIC, localName, lv.desc, null, null));
			}
		}

		cn.methods.add(createTransformedConstructor(cn, method));

		MethodNode mn = new MethodNode(ACC_PUBLIC, "run", Type.getMethodType(Type.VOID_TYPE, Object_TYPE).getDescriptor(), null, null);

		//mn.localVariables.add(0, new LocalVariableNode("this", cn.name, ));

		mn.instructions.add(method.instructions);
		mn.tryCatchBlocks = method.tryCatchBlocks;
		//mn.tryCatchBlocks


		//mn.localVariables.add(new LocalVariableNode("this", classType.getDescriptor(), cn.signature, (LabelNode)mn.instructions.getFirst(), (LabelNode)mn.instructions.getLast(), 0));

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

		for (AnalyzedFrame af : awaitAnalyzer.prepare(mn.instructions)) {
			AbstractInsnNode node = af.instruction;
			Frame frame = af.frame;

			//System.out.println(frame);
			if (node instanceof VarInsnNode) {
				boolean isNodeWrite = false;

				VarInsnNode varNode = (VarInsnNode) node;

				switch (varNode.getOpcode()) {
					case ILOAD:
					case LLOAD:
					case FLOAD:
					case DLOAD:
					case ALOAD:
						isNodeWrite = false;
						break;
					case ISTORE:
					case LSTORE:
					case FSTORE:
					case DSTORE:
					case ASTORE:
						isNodeWrite = true;
						break;
					default:
						throw (new Exception("Can't handle opcode"));
				}
				//LocalVariableNode localVar;
				String localVarName;
				String localVarDesc;
				if (varNode.var >= localsByIndex.length) {
					// Probably a throwable that is injected into catch and finally blocks
					// @TODO: check try...catch blocks to assert this
					Type type = Type.getType(Throwable.class);
					String localName = "throw_" + varNode.var;
					String fieldName = "local_" + localName;
					FieldNode field = ClassNodeUtils.getField(cn, fieldName);
					if (field == null) {
						cn.fields.add(new FieldNode(ACC_PUBLIC, fieldName, type.getDescriptor(), null, null));
					}
					localVarName = localName;
					localVarDesc = type.getDescriptor();
				} else {
					LocalVariableNode localVar = (LocalVariableNode) af.next.locals[varNode.var];
					LocalVariableNode localVar2 = (LocalVariableNode) localsByIndex[varNode.var];
					//System.out.println("local:" + localVar.name + ", " + ((localVar2 != null) ? localVar2.name : "--"));
					//LocalVariableNode localVar = af.locals[varNode.var];
					localVarName = localVar.name;
					localVarDesc = localVar.desc;
				}
				InsnList list = new InsnList();
				//System.out.println(localVar.name);

				if (isNodeWrite) {
					list.add(new VarInsnNode(ALOAD, 0));
					if (Type.getType(localVarDesc).getSize() == 2) {
						list.add(new InsnNode(DUP_X2));
						list.add(new InsnNode(POP));
					} else {
						list.add(new InsnNode(SWAP));
					}
					list.add(new FieldInsnNode(PUTFIELD, cn.name, "local_" + localVarName, localVarDesc));
				} else {
					list.add(new VarInsnNode(ALOAD, 0));
					list.add(new FieldInsnNode(GETFIELD, cn.name, "local_" + localVarName, localVarDesc));
				}

				mn.instructions.insertBefore(node, list);
				mn.instructions.remove(node);
				//System.out.println(varNode);
			}
			if (node instanceof FieldInsnNode) {
				FieldInsnNode fieldNode = (FieldInsnNode) node;
				if (fieldNode.owner.equals(outerClass.name)) {
					FieldNode field = getField(outerClass, fieldNode.name);
					if ((field.access & (ACC_PRIVATE | ACC_PROTECTED)) != 0) {
						// Must create or use an utility method for accessing that field without visibility access
						//System.out.println(field);
						MethodNode accessPrivateMethod = null;
						switch (fieldNode.getOpcode()) {
							case GETSTATIC:
							case GETFIELD:
								accessPrivateMethod = getOrCreateFieldAccessMethod(outerClassModify, field.name, false);
								break;
							case PUTSTATIC:
							case PUTFIELD:
								accessPrivateMethod = getOrCreateFieldAccessMethod(outerClassModify, field.name, true);
								break;
						}
						mn.instructions.insertBefore(node, new MethodInsnNode(INVOKESTATIC, outerClass.name, accessPrivateMethod.name, accessPrivateMethod.desc, false));
						mn.instructions.remove(node);
					}
				}
			}
			if (AwaitTools.isAwaitMethodCall(node)) {
				//System.out.println("await!");
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new MethodInsnNode(INVOKEVIRTUAL, Promise_TYPE.getInternalName(), "then", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ResultRunnable.class)), false));
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new IntInsnNode(BIPUSH, stateLabelNodes.size()));
				list.add(new FieldInsnNode(PUTFIELD, cn.name, "state", "I"));

				// Backup stack
				FieldNode[] restoreStackFields = null;

				Frame storeRestoreFrame = af.frame;
				if (storeRestoreFrame.getStackSize() >= 2) {

					restoreStackFields = new FieldNode[storeRestoreFrame.getStackSize() - 1];
					//restoreStackFields = new FieldNode[1];
					for (int m = restoreStackFields.length - 1; m >= 0; m--) {
						cn.fields.add(restoreStackFields[m] = new FieldNode(ACC_PRIVATE, "$$" + incrementalNameIndex++, ((TypeValue) storeRestoreFrame.getStack(m)).getType().getDescriptor(), null, null));
						list.add(new VarInsnNode(ALOAD, 0));

						if (Type.getType(restoreStackFields[m].desc).getSize() == 2) {
							list.add(new InsnNode(DUP_X2));
							list.add(new InsnNode(POP));
						} else {
							list.add(new InsnNode(SWAP));
						}

						list.add(new FieldInsnNode(PUTFIELD, cn.name, restoreStackFields[m].name, restoreStackFields[m].desc));
					}
				}

				list.add(getReturn(Type.VOID_TYPE));
				LabelNode awaitLabel = new LabelNode();
				stateLabelNodes.add(awaitLabel);
				list.add(awaitLabel);

				if (restoreStackFields != null) {
					for (int m = 0; m < restoreStackFields.length; m++) {
						list.add(new VarInsnNode(ALOAD, 0));
						list.add(new FieldInsnNode(GETFIELD, cn.name, restoreStackFields[m].name, restoreStackFields[m].desc));
					}
				}

				LabelNode skip_throw_label = new LabelNode();

				if (true) {
					//if (false) {
					list.add(new VarInsnNode(ALOAD, 1)); // Put the result of the promise here. (first parameter received)
					list.add(new TypeInsnNode(INSTANCEOF, Type.getType(Throwable.class).getInternalName()));
					list.add(new JumpInsnNode(IFEQ, skip_throw_label));
					{
						list.add(new VarInsnNode(ALOAD, 1)); // Put the result of the promise here. (first parameter received)
						list.add(new TypeInsnNode(CHECKCAST, Type.getType(Throwable.class).getInternalName()));
						list.add(new InsnNode(ATHROW));
					}
					list.add(skip_throw_label);
				}

				list.add(new VarInsnNode(ALOAD, 1));

				mn.instructions.insertBefore(node, list);
				mn.instructions.remove(node);
			}

			if (AwaitTools.isCompleteMethodCall(node)) {
				InsnList list = new InsnList();
				list.add(new IntInsnNode(ALOAD, 0));
				list.add(new FieldInsnNode(GETFIELD, cn.name, "promise", Promise_TYPE.getDescriptor()));
				list.add(new InsnNode(SWAP));
				list.add(new MethodInsnNode(INVOKEVIRTUAL, Promise_TYPE.getInternalName(), "resolve", Type.getMethodDescriptor(Type.VOID_TYPE, Object_TYPE), false));

				mn.instructions.insertBefore(node, list);
				mn.instructions.remove(node);
			}

			if (node instanceof MethodInsnNode) {
				MethodInsnNode methodNode = (MethodInsnNode) node;
				if (methodNode.owner.equals(outerClass.name)) {
					MethodNode method2 = ClassNodeUtils.getMethod(outerClass, methodNode.name, methodNode.desc);
					if ((method2.access & (ACC_PRIVATE | ACC_PROTECTED)) != 0) {
						MethodNode method3 = this.getOrCreateMethodAccessMethod(outerClassModify, method2);

						mn.instructions.insertBefore(node, new MethodInsnNode(INVOKESTATIC, outerClassModify.name, method3.name, method3.desc, false));
						mn.instructions.remove(node);
					}
				}
			}

			switch (node.getOpcode()) {
				case ARETURN:
				case IRETURN:
				case LRETURN:
				case FRETURN:
				case DRETURN:
					mn.instructions.insertBefore(node, getReturn(Type.VOID_TYPE));
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

		//System.out.println(cn.sourceFile);
		//for (Object node : mn.instructions.toArray()) System.out.println(ClassNodeUtils.toString((AbstractInsnNode)node));

		cn.methods.add(mn);

		return cn;
	}

	public boolean processFile(SVfsFile classFile) throws Exception {
		SVfsFile originalClassFile = classFile.getVfs().access(classFile.getName() + ".original");

		if ((classFile.lastModified() != originalClassFile.lastModified())) {
			//System.out.println("COPIED! " + originalClassFile.exists() + " " + classFile.lastModified() + " " + originalClassFile.lastModified());

			originalClassFile.write(classFile.read());
		}

		byte[] originalClassBytes = originalClassFile.read();

		if (!AwaitTools.classReferencesPromises(originalClassBytes)) {
			return false;
		}

		ClassNode clazz = getClassFromBytes(originalClassBytes);
		ClassNode clazz2 = getClassFromBytes(originalClassBytes);
		clazz.version = V1_8;

		int awaitMethodCount = 0;

		for (Object _method : clazz.methods.toArray()) {
			MethodNode method = (MethodNode) _method;

			if (AwaitTools.hasAwait(method)) {
				awaitMethodCount++;
				int argumentCountIncludingThis = AwaitTools.getMethodArgumentCountIncludingThis(method);
				//System.out.println("argumentCountIncludingThis:" + argumentCountIncludingThis);

				//System.out.println("Method with await! " + method.name);

				method.instructions = new InsnList();
				method.tryCatchBlocks = new LinkedList<>();

				MethodNode method2 = ClassNodeUtils.getMethod(clazz2, method.name, method.desc);

				//System.out.println(method2.name);
				ClassNode runClass = createTransformedClassForMethod(clazz2, clazz, method2);
				//System.out.println(outputFile.getParent());

				//System.out.println(runClass.name + ".class");
				classFile.getVfs().access(runClass.name + ".class").write(AwaitTools.getClassBytes(runClass));
				method.instructions.add(new TypeInsnNode(NEW, runClass.name));
				method.instructions.add(new InsnNode(DUP));
				MethodNode mnInit = (MethodNode) runClass.methods.get(0);
				MethodNode mnRun = (MethodNode) runClass.methods.get(1);
				//System.out.println(argumentCountIncludingThis + ";" + method.name + ";" + clazz.name);

				boolean isStatic = isStatic(method);
				Type initType = Type.getMethodType(mnInit.desc);
				Type[] initArguments = initType.getArgumentTypes();
				for (int n = 0; n < initArguments.length; n++) {
					method.instructions.add(getLoad(initArguments[n], n));
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
			AwaitTools.writeOriginalClass(clazz, originalClassBytes);

			classFile.write(AwaitTools.getClassBytes(clazz));
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
}
