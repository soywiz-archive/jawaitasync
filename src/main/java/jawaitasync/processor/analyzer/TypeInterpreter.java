/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package jawaitasync.processor.analyzer;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

import java.util.List;

/**
 * An {@link org.objectweb.asm.tree.analysis.Interpreter} for {@link TypeValue} values.
 *
 * @author Eric Bruneton
 * @author Bing Ran
 */
public class TypeInterpreter extends Interpreter implements Opcodes {

	public TypeInterpreter() {
		super(ASM5);
	}

	protected TypeInterpreter(final int api) {
		super(api);
	}

	@Override
	public Value newValue(final Type type) {
		if (type == null) {
			return TypeValue.UNINITIALIZED_VALUE;
		}
		switch (type.getSort()) {
			case Type.VOID:
				return null;
			case Type.BOOLEAN:
			case Type.CHAR:
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
				return TypeValue.INT_VALUE;
			case Type.FLOAT:
				return TypeValue.FLOAT_VALUE;
			case Type.LONG:
				return TypeValue.LONG_VALUE;
			case Type.DOUBLE:
				return TypeValue.DOUBLE_VALUE;
			case Type.ARRAY:
			case Type.OBJECT:
				return new TypeValue(type);
			default:
				throw new Error("Internal error");
		}
	}

	@Override
	public Value newOperation(final AbstractInsnNode insn)
		throws AnalyzerException {
		switch (insn.getOpcode()) {
			case ACONST_NULL:
				return newValue(Type.getObjectType("null"));
			case ICONST_M1:
			case ICONST_0:
			case ICONST_1:
			case ICONST_2:
			case ICONST_3:
			case ICONST_4:
			case ICONST_5:
				return TypeValue.INT_VALUE;
			case LCONST_0:
			case LCONST_1:
				return TypeValue.LONG_VALUE;
			case FCONST_0:
			case FCONST_1:
			case FCONST_2:
				return TypeValue.FLOAT_VALUE;
			case DCONST_0:
			case DCONST_1:
				return TypeValue.DOUBLE_VALUE;
			case BIPUSH:
			case SIPUSH:
				return TypeValue.INT_VALUE;
			case LDC:
				Object cst = ((LdcInsnNode) insn).cst;
				if (cst instanceof Integer) {
					return TypeValue.INT_VALUE;
				} else if (cst instanceof Float) {
					return TypeValue.FLOAT_VALUE;
				} else if (cst instanceof Long) {
					return TypeValue.LONG_VALUE;
				} else if (cst instanceof Double) {
					return TypeValue.DOUBLE_VALUE;
				} else if (cst instanceof String) {
					return newValue(Type.getObjectType("java/lang/String"));
				} else if (cst instanceof Type) {
					int sort = ((Type) cst).getSort();
					if (sort == Type.OBJECT || sort == Type.ARRAY) {
						return newValue(Type.getObjectType("java/lang/Class"));
					} else if (sort == Type.METHOD) {
						return newValue(Type
							.getObjectType("java/lang/invoke/MethodType"));
					} else {
						throw new IllegalArgumentException("Illegal LDC constant "
							+ cst);
					}
				} else if (cst instanceof Handle) {
					return newValue(Type
						.getObjectType("java/lang/invoke/MethodHandle"));
				} else {
					throw new IllegalArgumentException("Illegal LDC constant "
						+ cst);
				}
			case JSR:
				return TypeValue.RETURNADDRESS_VALUE;
			case GETSTATIC:
				return newValue(Type.getType(((FieldInsnNode) insn).desc));
			case NEW:
				return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
			default:
				throw new Error("Internal error.");
		}
	}

	@Override
	public Value copyOperation(final AbstractInsnNode insn,
	                               final Value value) throws AnalyzerException {
		return value;
	}

	@Override
	public Value unaryOperation(final AbstractInsnNode insn,
	                                final Value value) throws AnalyzerException {
		switch (insn.getOpcode()) {
			case INEG:
			case IINC:
			case L2I:
			case F2I:
			case D2I:
			case I2B:
			case I2C:
			case I2S:
				return TypeValue.INT_VALUE;
			case FNEG:
			case I2F:
			case L2F:
			case D2F:
				return TypeValue.FLOAT_VALUE;
			case LNEG:
			case I2L:
			case F2L:
			case D2L:
				return TypeValue.LONG_VALUE;
			case DNEG:
			case I2D:
			case L2D:
			case F2D:
				return TypeValue.DOUBLE_VALUE;
			case IFEQ:
			case IFNE:
			case IFLT:
			case IFGE:
			case IFGT:
			case IFLE:
			case TABLESWITCH:
			case LOOKUPSWITCH:
			case IRETURN:
			case LRETURN:
			case FRETURN:
			case DRETURN:
			case ARETURN:
			case PUTSTATIC:
				return null;
			case GETFIELD:
				return newValue(Type.getType(((FieldInsnNode) insn).desc));
			case NEWARRAY:
				switch (((IntInsnNode) insn).operand) {
					case T_BOOLEAN:
						return newValue(Type.getType("[Z"));
					case T_CHAR:
						return newValue(Type.getType("[C"));
					case T_BYTE:
						return newValue(Type.getType("[B"));
					case T_SHORT:
						return newValue(Type.getType("[S"));
					case T_INT:
						return newValue(Type.getType("[I"));
					case T_FLOAT:
						return newValue(Type.getType("[F"));
					case T_DOUBLE:
						return newValue(Type.getType("[D"));
					case T_LONG:
						return newValue(Type.getType("[J"));
					default:
						throw new AnalyzerException(insn, "Invalid array type");
				}
			case ANEWARRAY:
				String desc = ((TypeInsnNode) insn).desc;
				return newValue(Type.getType("[" + Type.getObjectType(desc)));
			case ARRAYLENGTH:
				return TypeValue.INT_VALUE;
			case ATHROW:
				return null;
			case CHECKCAST:
				desc = ((TypeInsnNode) insn).desc;
				return newValue(Type.getObjectType(desc));
			case INSTANCEOF:
				return TypeValue.INT_VALUE;
			case MONITORENTER:
			case MONITOREXIT:
			case IFNULL:
			case IFNONNULL:
				return null;
			default:
				throw new Error("Internal error.");
		}
	}

	@Override
	public Value binaryOperation(final AbstractInsnNode insn,
	                                 final Value value1, final Value value2)
		throws AnalyzerException {
		switch (insn.getOpcode()) {
			case IALOAD:
			case BALOAD:
			case CALOAD:
			case SALOAD:
			case IADD:
			case ISUB:
			case IMUL:
			case IDIV:
			case IREM:
			case ISHL:
			case ISHR:
			case IUSHR:
			case IAND:
			case IOR:
			case IXOR:
				return TypeValue.INT_VALUE;
			case FALOAD:
			case FADD:
			case FSUB:
			case FMUL:
			case FDIV:
			case FREM:
				return TypeValue.FLOAT_VALUE;
			case LALOAD:
			case LADD:
			case LSUB:
			case LMUL:
			case LDIV:
			case LREM:
			case LSHL:
			case LSHR:
			case LUSHR:
			case LAND:
			case LOR:
			case LXOR:
				return TypeValue.LONG_VALUE;
			case DALOAD:
			case DADD:
			case DSUB:
			case DMUL:
			case DDIV:
			case DREM:
				return TypeValue.DOUBLE_VALUE;
			case AALOAD:
				return new TypeValue(((TypeValue)value1).getType().getElementType());
			case LCMP:
			case FCMPL:
			case FCMPG:
			case DCMPL:
			case DCMPG:
				return TypeValue.INT_VALUE;
			case IF_ICMPEQ:
			case IF_ICMPNE:
			case IF_ICMPLT:
			case IF_ICMPGE:
			case IF_ICMPGT:
			case IF_ICMPLE:
			case IF_ACMPEQ:
			case IF_ACMPNE:
			case PUTFIELD:
				return null;
			default:
				throw new Error("Internal error.");
		}
	}

	@Override
	public Value ternaryOperation(final AbstractInsnNode insn,
	                                  final Value value1, final Value value2,
	                                  final Value value3) throws AnalyzerException {
		return null;
	}

	@Override
	public Value naryOperation(final AbstractInsnNode insn,
	                               final List values) throws AnalyzerException {
		int opcode = insn.getOpcode();
		if (opcode == MULTIANEWARRAY) {
			return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
		} else if (opcode == INVOKEDYNAMIC) {
			return newValue(Type
				.getReturnType(((InvokeDynamicInsnNode) insn).desc));
		} else {
			return newValue(Type.getReturnType(((MethodInsnNode) insn).desc));
		}
	}

	@Override
	public void returnOperation(final AbstractInsnNode insn,
	                            final Value value, final Value expected)
		throws AnalyzerException {
	}

	@Override
	public TypeValue merge(final Value _v, final Value _w) {
		TypeValue v = (TypeValue)_v;
		TypeValue w = (TypeValue)_w;
		if (!v.equals(w)) {
			return TypeValue.UNINITIALIZED_VALUE;
		}
		return v;
	}
}
