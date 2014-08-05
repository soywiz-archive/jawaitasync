package jawaitasync.processor;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.naming.NameNotFoundException;
import java.util.HashMap;

import static org.objectweb.asm.Opcodes.*;

public class ClassNodeUtils {
	static public MethodNode getMethod(ClassNode cn, String name) throws Exception {
		for (MethodNode mn : (MethodNode[]) cn.methods.toArray(new MethodNode[0])) if (mn.name.equals(name)) return mn;
		throw (new NameNotFoundException("Can't find method " + name));
	}

	static public MethodNode getMethod(ClassNode cn, String name, String desc) throws Exception {
		for (MethodNode mn : (MethodNode[]) cn.methods.toArray(new MethodNode[0]))
			if (mn.name.equals(name) && mn.desc.equals(desc)) return mn;
		throw (new NameNotFoundException("Can't find method " + name + " | " + desc));
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

	public static String toString(AbstractInsnNode node) {
		if (node instanceof LabelNode) return ":: " + ((LabelNode) node).getLabel().toString() + ":";
		if (node instanceof LineNumberNode) return "Line:" + ((LineNumberNode) node).line;
		if (node instanceof VarInsnNode) {
			VarInsnNode varNode = (VarInsnNode) node;
			return opcodeNamesById.get(varNode.getOpcode()) + ":" + varNode.var;
		}
		if (node instanceof IntInsnNode) {
			IntInsnNode intNode = (IntInsnNode) node;
			return opcodeNamesById.get(intNode.getOpcode()) + ":" + intNode.operand;
		}
		if (node instanceof TypeInsnNode) {
			TypeInsnNode typeNode = (TypeInsnNode) node;
			return opcodeNamesById.get(typeNode.getOpcode()) + ":" + typeNode.desc;
		}
		if (node instanceof JumpInsnNode) {
			JumpInsnNode jumpNode = (JumpInsnNode) node;
			return opcodeNamesById.get(jumpNode.getOpcode()) + ":" + jumpNode.label.getLabel();
		}
		if (node instanceof FrameNode) {
			FrameNode frameNode = (FrameNode) node;
			return "FRAME:" + frameNode;
		}
		if (node instanceof LookupSwitchInsnNode) {
			LookupSwitchInsnNode lookupNode = (LookupSwitchInsnNode) node;
			String out = "";
			for (int n = 0; n < lookupNode.keys.size(); n++) {
				out = out + lookupNode.keys.get(n) + ":" + ((LabelNode)lookupNode.labels.get(n)).getLabel() + ", ";
			}
			return "LOOKUPSWITCH:" + "" + lookupNode.dflt.getLabel() + " :: " + out;
		}
		if (node instanceof FieldInsnNode) {
			FieldInsnNode fieldNode = (FieldInsnNode) node;
			return opcodeNamesById.get(fieldNode.getOpcode()) + ":" + fieldNode.name;
		}
		if (node instanceof InsnNode) {
			InsnNode _node = (InsnNode) node;
			return opcodeNamesById.get(_node.getOpcode());
		}
		if (node instanceof MethodInsnNode) {
			MethodInsnNode methodNode = (MethodInsnNode) node;
			return opcodeNamesById.get(methodNode.getOpcode()) + ":" + methodNode.owner + "/" + methodNode.name;
		}
		if (node instanceof LdcInsnNode) {
			LdcInsnNode ldcNode = (LdcInsnNode) node;
			return opcodeNamesById.get(ldcNode.getOpcode()) + ":" + ldcNode.cst;
		}
		return "--" + node.toString();
	}

	static final HashMap<Integer, String> opcodeNamesById = new HashMap<Integer, String>();

	static {
		addOpcode("NOP", NOP, OpcodeGroup.INSN);
		addOpcode("ACONST_NULL", ACONST_NULL, OpcodeGroup.INSN);
		addOpcode("ICONST_M1", ICONST_M1, OpcodeGroup.INSN);
		addOpcode("ICONST_0", ICONST_0, OpcodeGroup.INSN);
		addOpcode("ICONST_1", ICONST_1, OpcodeGroup.INSN);
		addOpcode("ICONST_2", ICONST_2, OpcodeGroup.INSN);
		addOpcode("ICONST_3", ICONST_3, OpcodeGroup.INSN);
		addOpcode("ICONST_4", ICONST_4, OpcodeGroup.INSN);
		addOpcode("ICONST_5", ICONST_5, OpcodeGroup.INSN);
		addOpcode("LCONST_0", LCONST_0, OpcodeGroup.INSN);
		addOpcode("LCONST_1", LCONST_1, OpcodeGroup.INSN);
		addOpcode("FCONST_0", FCONST_0, OpcodeGroup.INSN);
		addOpcode("FCONST_1", FCONST_1, OpcodeGroup.INSN);
		addOpcode("FCONST_2", FCONST_2, OpcodeGroup.INSN);
		addOpcode("DCONST_0", DCONST_0, OpcodeGroup.INSN);
		addOpcode("DCONST_1", DCONST_1, OpcodeGroup.INSN);
		addOpcode("BIPUSH", BIPUSH, OpcodeGroup.INSN_INT);
		addOpcode("SIPUSH", SIPUSH, OpcodeGroup.INSN_INT);
		addOpcode("LDC", LDC, OpcodeGroup.INSN_LDC);
		addOpcode("ILOAD", ILOAD, OpcodeGroup.INSN_VAR);
		addOpcode("LLOAD", LLOAD, OpcodeGroup.INSN_VAR);
		addOpcode("FLOAD", FLOAD, OpcodeGroup.INSN_VAR);
		addOpcode("DLOAD", DLOAD, OpcodeGroup.INSN_VAR);
		addOpcode("ALOAD", ALOAD, OpcodeGroup.INSN_VAR);
		addOpcode("IALOAD", IALOAD, OpcodeGroup.INSN);
		addOpcode("LALOAD", LALOAD, OpcodeGroup.INSN);
		addOpcode("FALOAD", FALOAD, OpcodeGroup.INSN);
		addOpcode("DALOAD", DALOAD, OpcodeGroup.INSN);
		addOpcode("AALOAD", AALOAD, OpcodeGroup.INSN);
		addOpcode("BALOAD", BALOAD, OpcodeGroup.INSN);
		addOpcode("CALOAD", CALOAD, OpcodeGroup.INSN);
		addOpcode("SALOAD", SALOAD, OpcodeGroup.INSN);
		addOpcode("ISTORE", ISTORE, OpcodeGroup.INSN_VAR);
		addOpcode("LSTORE", LSTORE, OpcodeGroup.INSN_VAR);
		addOpcode("FSTORE", FSTORE, OpcodeGroup.INSN_VAR);
		addOpcode("DSTORE", DSTORE, OpcodeGroup.INSN_VAR);
		addOpcode("ASTORE", ASTORE, OpcodeGroup.INSN_VAR);
		addOpcode("IASTORE", IASTORE, OpcodeGroup.INSN);
		addOpcode("LASTORE", LASTORE, OpcodeGroup.INSN);
		addOpcode("FASTORE", FASTORE, OpcodeGroup.INSN);
		addOpcode("DASTORE", DASTORE, OpcodeGroup.INSN);
		addOpcode("AASTORE", AASTORE, OpcodeGroup.INSN);
		addOpcode("BASTORE", BASTORE, OpcodeGroup.INSN);
		addOpcode("CASTORE", CASTORE, OpcodeGroup.INSN);
		addOpcode("SASTORE", SASTORE, OpcodeGroup.INSN);
		addOpcode("POP", POP, OpcodeGroup.INSN);
		addOpcode("POP2", POP2, OpcodeGroup.INSN);
		addOpcode("DUP", DUP, OpcodeGroup.INSN);
		addOpcode("DUP_X1", DUP_X1, OpcodeGroup.INSN);
		addOpcode("DUP_X2", DUP_X2, OpcodeGroup.INSN);
		addOpcode("DUP2", DUP2, OpcodeGroup.INSN);
		addOpcode("DUP2_X1", DUP2_X1, OpcodeGroup.INSN);
		addOpcode("DUP2_X2", DUP2_X2, OpcodeGroup.INSN);
		addOpcode("SWAP", SWAP, OpcodeGroup.INSN);
		addOpcode("IADD", IADD, OpcodeGroup.INSN);
		addOpcode("LADD", LADD, OpcodeGroup.INSN);
		addOpcode("FADD", FADD, OpcodeGroup.INSN);
		addOpcode("DADD", DADD, OpcodeGroup.INSN);
		addOpcode("ISUB", ISUB, OpcodeGroup.INSN);
		addOpcode("LSUB", LSUB, OpcodeGroup.INSN);
		addOpcode("FSUB", FSUB, OpcodeGroup.INSN);
		addOpcode("DSUB", DSUB, OpcodeGroup.INSN);
		addOpcode("IMUL", IMUL, OpcodeGroup.INSN);
		addOpcode("LMUL", LMUL, OpcodeGroup.INSN);
		addOpcode("FMUL", FMUL, OpcodeGroup.INSN);
		addOpcode("DMUL", DMUL, OpcodeGroup.INSN);
		addOpcode("IDIV", IDIV, OpcodeGroup.INSN);
		addOpcode("LDIV", LDIV, OpcodeGroup.INSN);
		addOpcode("FDIV", FDIV, OpcodeGroup.INSN);
		addOpcode("DDIV", DDIV, OpcodeGroup.INSN);
		addOpcode("IREM", IREM, OpcodeGroup.INSN);
		addOpcode("LREM", LREM, OpcodeGroup.INSN);
		addOpcode("FREM", FREM, OpcodeGroup.INSN);
		addOpcode("DREM", DREM, OpcodeGroup.INSN);
		addOpcode("INEG", INEG, OpcodeGroup.INSN);
		addOpcode("LNEG", LNEG, OpcodeGroup.INSN);
		addOpcode("FNEG", FNEG, OpcodeGroup.INSN);
		addOpcode("DNEG", DNEG, OpcodeGroup.INSN);
		addOpcode("ISHL", ISHL, OpcodeGroup.INSN);
		addOpcode("LSHL", LSHL, OpcodeGroup.INSN);
		addOpcode("ISHR", ISHR, OpcodeGroup.INSN);
		addOpcode("LSHR", LSHR, OpcodeGroup.INSN);
		addOpcode("IUSHR", IUSHR, OpcodeGroup.INSN);
		addOpcode("LUSHR", LUSHR, OpcodeGroup.INSN);
		addOpcode("IAND", IAND, OpcodeGroup.INSN);
		addOpcode("LAND", LAND, OpcodeGroup.INSN);
		addOpcode("IOR", IOR, OpcodeGroup.INSN);
		addOpcode("LOR", LOR, OpcodeGroup.INSN);
		addOpcode("IXOR", IXOR, OpcodeGroup.INSN);
		addOpcode("LXOR", LXOR, OpcodeGroup.INSN);
		addOpcode("IINC", IINC, OpcodeGroup.INSN_IINC);
		addOpcode("I2L", I2L, OpcodeGroup.INSN);
		addOpcode("I2F", I2F, OpcodeGroup.INSN);
		addOpcode("I2D", I2D, OpcodeGroup.INSN);
		addOpcode("L2I", L2I, OpcodeGroup.INSN);
		addOpcode("L2F", L2F, OpcodeGroup.INSN);
		addOpcode("L2D", L2D, OpcodeGroup.INSN);
		addOpcode("F2I", F2I, OpcodeGroup.INSN);
		addOpcode("F2L", F2L, OpcodeGroup.INSN);
		addOpcode("F2D", F2D, OpcodeGroup.INSN);
		addOpcode("D2I", D2I, OpcodeGroup.INSN);
		addOpcode("D2L", D2L, OpcodeGroup.INSN);
		addOpcode("D2F", D2F, OpcodeGroup.INSN);
		addOpcode("I2B", I2B, OpcodeGroup.INSN);
		addOpcode("I2C", I2C, OpcodeGroup.INSN);
		addOpcode("I2S", I2S, OpcodeGroup.INSN);
		addOpcode("LCMP", LCMP, OpcodeGroup.INSN);
		addOpcode("FCMPL", FCMPL, OpcodeGroup.INSN);
		addOpcode("FCMPG", FCMPG, OpcodeGroup.INSN);
		addOpcode("DCMPL", DCMPL, OpcodeGroup.INSN);
		addOpcode("DCMPG", DCMPG, OpcodeGroup.INSN);
		addOpcode("IFEQ", IFEQ, OpcodeGroup.INSN_JUMP);
		addOpcode("IFNE", IFNE, OpcodeGroup.INSN_JUMP);
		addOpcode("IFLT", IFLT, OpcodeGroup.INSN_JUMP);
		addOpcode("IFGE", IFGE, OpcodeGroup.INSN_JUMP);
		addOpcode("IFGT", IFGT, OpcodeGroup.INSN_JUMP);
		addOpcode("IFLE", IFLE, OpcodeGroup.INSN_JUMP);
		addOpcode("IF_ICMPEQ", IF_ICMPEQ, OpcodeGroup.INSN_JUMP);
		addOpcode("IF_ICMPNE", IF_ICMPNE, OpcodeGroup.INSN_JUMP);
		addOpcode("IF_ICMPLT", IF_ICMPLT, OpcodeGroup.INSN_JUMP);
		addOpcode("IF_ICMPGE", IF_ICMPGE, OpcodeGroup.INSN_JUMP);
		addOpcode("IF_ICMPGT", IF_ICMPGT, OpcodeGroup.INSN_JUMP);
		addOpcode("IF_ICMPLE", IF_ICMPLE, OpcodeGroup.INSN_JUMP);
		addOpcode("IF_ACMPEQ", IF_ACMPEQ, OpcodeGroup.INSN_JUMP);
		addOpcode("IF_ACMPNE", IF_ACMPNE, OpcodeGroup.INSN_JUMP);
		addOpcode("GOTO", GOTO, OpcodeGroup.INSN_JUMP);
		addOpcode("JSR", JSR, OpcodeGroup.INSN_JUMP);
		addOpcode("RET", RET, OpcodeGroup.INSN_VAR);
		addOpcode("IRETURN", IRETURN, OpcodeGroup.INSN);
		addOpcode("LRETURN", LRETURN, OpcodeGroup.INSN);
		addOpcode("FRETURN", FRETURN, OpcodeGroup.INSN);
		addOpcode("DRETURN", DRETURN, OpcodeGroup.INSN);
		addOpcode("ARETURN", ARETURN, OpcodeGroup.INSN);
		addOpcode("RETURN", RETURN, OpcodeGroup.INSN);
		addOpcode("GETSTATIC", GETSTATIC, OpcodeGroup.INSN_FIELD);
		addOpcode("PUTSTATIC", PUTSTATIC, OpcodeGroup.INSN_FIELD);
		addOpcode("GETFIELD", GETFIELD, OpcodeGroup.INSN_FIELD);
		addOpcode("PUTFIELD", PUTFIELD, OpcodeGroup.INSN_FIELD);
		addOpcode("INVOKEVIRTUAL", INVOKEVIRTUAL, OpcodeGroup.INSN_METHOD);
		addOpcode("INVOKESPECIAL", INVOKESPECIAL, OpcodeGroup.INSN_METHOD);
		addOpcode("INVOKESTATIC", INVOKESTATIC, OpcodeGroup.INSN_METHOD);
		addOpcode("INVOKEINTERFACE", INVOKEINTERFACE, OpcodeGroup.INSN_METHOD);
		addOpcode("NEW", NEW, OpcodeGroup.INSN_TYPE);
		addOpcode("NEWARRAY", NEWARRAY, OpcodeGroup.INSN_INT);
		addOpcode("ANEWARRAY", ANEWARRAY, OpcodeGroup.INSN_TYPE);
		addOpcode("ARRAYLENGTH", ARRAYLENGTH, OpcodeGroup.INSN);
		addOpcode("ATHROW", ATHROW, OpcodeGroup.INSN);
		addOpcode("CHECKCAST", CHECKCAST, OpcodeGroup.INSN_TYPE);
		addOpcode("INSTANCEOF", INSTANCEOF, OpcodeGroup.INSN_TYPE);
		addOpcode("MONITORENTER", MONITORENTER, OpcodeGroup.INSN);
		addOpcode("MONITOREXIT", MONITOREXIT, OpcodeGroup.INSN);
		addOpcode("MULTIANEWARRAY", MULTIANEWARRAY,
			OpcodeGroup.INSN_MULTIANEWARRAY);
		addOpcode("IFNULL", IFNULL, OpcodeGroup.INSN_JUMP);
		addOpcode("IFNONNULL", IFNONNULL, OpcodeGroup.INSN_JUMP);
	}

	private static void addOpcode(String operStr, int oper, OpcodeGroup group) {
		opcodeNamesById.put(oper, operStr);
	}

}

enum OpcodeGroup {
	INSN_JUMP,
	INSN,
	INSN_METHOD,
	INSN_FIELD,
	INSN_VAR,
	INSN_IINC,
	INSN_LDC,
	INSN_INT,
	INSN_TYPE,
	INSN_MULTIANEWARRAY,
}