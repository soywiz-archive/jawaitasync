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

import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Value;

/**
 * An extended {@link TypeInterpreter} that checks that bytecode instructions
 * are correctly used.
 * 
 * @author Eric Bruneton
 * @author Bing Ran
 */
public class TypeVerifier extends TypeInterpreter {

    public TypeVerifier() {
        super(ASM5);
    }

    protected TypeVerifier(final int api) {
        super(api);
    }

    @Override
    public TypeValue copyOperation(final AbstractInsnNode insn,
            final TypeValue value) throws AnalyzerException {
        Value expected;
        switch (insn.getOpcode()) {
        case ILOAD:
        case ISTORE:
            expected = TypeValue.INT_VALUE;
            break;
        case FLOAD:
        case FSTORE:
            expected = TypeValue.FLOAT_VALUE;
            break;
        case LLOAD:
        case LSTORE:
            expected = TypeValue.LONG_VALUE;
            break;
        case DLOAD:
        case DSTORE:
            expected = TypeValue.DOUBLE_VALUE;
            break;
        case ALOAD:
            if (!value.isReference()) {
                throw new AnalyzerException(insn, null, "an object reference",
                        value);
            }
            return value;
        case ASTORE:
            if (!value.isReference()
                    && !TypeValue.RETURNADDRESS_VALUE.equals(value)) {
                throw new AnalyzerException(insn, null,
                        "an object reference or a return address", value);
            }
            return value;
        default:
            return value;
        }
        if (!expected.equals(value)) {
            throw new AnalyzerException(insn, null, expected, value);
        }
        return value;
    }

    @Override
    public TypeValue unaryOperation(final AbstractInsnNode insn,
            final TypeValue value) throws AnalyzerException {
        TypeValue expected;
        switch (insn.getOpcode()) {
        case INEG:
        case IINC:
        case I2F:
        case I2L:
        case I2D:
        case I2B:
        case I2C:
        case I2S:
        case IFEQ:
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case TABLESWITCH:
        case LOOKUPSWITCH:
        case IRETURN:
        case NEWARRAY:
        case ANEWARRAY:
            expected = TypeValue.INT_VALUE;
            break;
        case FNEG:
        case F2I:
        case F2L:
        case F2D:
        case FRETURN:
            expected = TypeValue.FLOAT_VALUE;
            break;
        case LNEG:
        case L2I:
        case L2F:
        case L2D:
        case LRETURN:
            expected = TypeValue.LONG_VALUE;
            break;
        case DNEG:
        case D2I:
        case D2F:
        case D2L:
        case DRETURN:
            expected = TypeValue.DOUBLE_VALUE;
            break;
        case GETFIELD:
            expected = newValue(Type
                    .getObjectType(((FieldInsnNode) insn).owner));
            break;
        case CHECKCAST:
            if (!value.isReference()) {
                throw new AnalyzerException(insn, null, "an object reference",
                        value);
            }
            return super.unaryOperation(insn, value);
        case ARRAYLENGTH:
            if (!isArrayValue(value)) {
                throw new AnalyzerException(insn, null, "an array reference",
                        value);
            }
            return super.unaryOperation(insn, value);
        case ARETURN:
        case ATHROW:
        case INSTANCEOF:
        case MONITORENTER:
        case MONITOREXIT:
        case IFNULL:
        case IFNONNULL:
            if (!value.isReference()) {
                throw new AnalyzerException(insn, null, "an object reference",
                        value);
            }
            return super.unaryOperation(insn, value);
        case PUTSTATIC:
            expected = newValue(Type.getType(((FieldInsnNode) insn).desc));
            break;
        default:
            throw new Error("Internal error.");
        }
        if (!isSubTypeOf(value, expected)) {
            throw new AnalyzerException(insn, null, expected, value);
        }
        return super.unaryOperation(insn, value);
    }

    @Override
    public TypeValue binaryOperation(final AbstractInsnNode insn,
            final TypeValue value1, final TypeValue value2)
            throws AnalyzerException {
        TypeValue expected1;
        TypeValue expected2;
        switch (insn.getOpcode()) {
        case IALOAD:
            expected1 = newValue(Type.getType("[I"));
            expected2 = TypeValue.INT_VALUE;
            break;
        case BALOAD:
            if (isSubTypeOf(value1, newValue(Type.getType("[Z")))) {
                expected1 = newValue(Type.getType("[Z"));
            } else {
                expected1 = newValue(Type.getType("[B"));
            }
            expected2 = TypeValue.INT_VALUE;
            break;
        case CALOAD:
            expected1 = newValue(Type.getType("[C"));
            expected2 = TypeValue.INT_VALUE;
            break;
        case SALOAD:
            expected1 = newValue(Type.getType("[S"));
            expected2 = TypeValue.INT_VALUE;
            break;
        case LALOAD:
            expected1 = newValue(Type.getType("[J"));
            expected2 = TypeValue.INT_VALUE;
            break;
        case FALOAD:
            expected1 = newValue(Type.getType("[F"));
            expected2 = TypeValue.INT_VALUE;
            break;
        case DALOAD:
            expected1 = newValue(Type.getType("[D"));
            expected2 = TypeValue.INT_VALUE;
            break;
        case AALOAD:
            expected1 = newValue(Type.getType("[Ljava/lang/Object;"));
            expected2 = TypeValue.INT_VALUE;
            break;
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
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
            expected1 = TypeValue.INT_VALUE;
            expected2 = TypeValue.INT_VALUE;
            break;
        case FADD:
        case FSUB:
        case FMUL:
        case FDIV:
        case FREM:
        case FCMPL:
        case FCMPG:
            expected1 = TypeValue.FLOAT_VALUE;
            expected2 = TypeValue.FLOAT_VALUE;
            break;
        case LADD:
        case LSUB:
        case LMUL:
        case LDIV:
        case LREM:
        case LAND:
        case LOR:
        case LXOR:
        case LCMP:
            expected1 = TypeValue.LONG_VALUE;
            expected2 = TypeValue.LONG_VALUE;
            break;
        case LSHL:
        case LSHR:
        case LUSHR:
            expected1 = TypeValue.LONG_VALUE;
            expected2 = TypeValue.INT_VALUE;
            break;
        case DADD:
        case DSUB:
        case DMUL:
        case DDIV:
        case DREM:
        case DCMPL:
        case DCMPG:
            expected1 = TypeValue.DOUBLE_VALUE;
            expected2 = TypeValue.DOUBLE_VALUE;
            break;
        case IF_ACMPEQ:
        case IF_ACMPNE:
            expected1 = TypeValue.REFERENCE_VALUE;
            expected2 = TypeValue.REFERENCE_VALUE;
            break;
        case PUTFIELD:
            FieldInsnNode fin = (FieldInsnNode) insn;
            expected1 = newValue(Type.getObjectType(fin.owner));
            expected2 = newValue(Type.getType(fin.desc));
            break;
        default:
            throw new Error("Internal error.");
        }
        if (!isSubTypeOf(value1, expected1)) {
            throw new AnalyzerException(insn, "First argument", expected1,
                    value1);
        } else if (!isSubTypeOf(value2, expected2)) {
            throw new AnalyzerException(insn, "Second argument", expected2,
                    value2);
        }
        if (insn.getOpcode() == AALOAD) {
            return getElementValue(value1);
        } else {
            return super.binaryOperation(insn, value1, value2);
        }
    }

    @Override
    public TypeValue ternaryOperation(final AbstractInsnNode insn,
            final TypeValue value1, final TypeValue value2,
            final TypeValue value3) throws AnalyzerException {
        TypeValue expected1;
        TypeValue expected3;
        switch (insn.getOpcode()) {
        case IASTORE:
            expected1 = newValue(Type.getType("[I"));
            expected3 = TypeValue.INT_VALUE;
            break;
        case BASTORE:
            if (isSubTypeOf(value1, newValue(Type.getType("[Z")))) {
                expected1 = newValue(Type.getType("[Z"));
            } else {
                expected1 = newValue(Type.getType("[B"));
            }
            expected3 = TypeValue.INT_VALUE;
            break;
        case CASTORE:
            expected1 = newValue(Type.getType("[C"));
            expected3 = TypeValue.INT_VALUE;
            break;
        case SASTORE:
            expected1 = newValue(Type.getType("[S"));
            expected3 = TypeValue.INT_VALUE;
            break;
        case LASTORE:
            expected1 = newValue(Type.getType("[J"));
            expected3 = TypeValue.LONG_VALUE;
            break;
        case FASTORE:
            expected1 = newValue(Type.getType("[F"));
            expected3 = TypeValue.FLOAT_VALUE;
            break;
        case DASTORE:
            expected1 = newValue(Type.getType("[D"));
            expected3 = TypeValue.DOUBLE_VALUE;
            break;
        case AASTORE:
            expected1 = value1;
            expected3 = TypeValue.REFERENCE_VALUE;
            break;
        default:
            throw new Error("Internal error.");
        }
        if (!isSubTypeOf(value1, expected1)) {
            throw new AnalyzerException(insn, "First argument", "a "
                    + expected1 + " array reference", value1);
        } else if (!TypeValue.INT_VALUE.equals(value2)) {
            throw new AnalyzerException(insn, "Second argument",
                    TypeValue.INT_VALUE, value2);
        } else if (!isSubTypeOf(value3, expected3)) {
            throw new AnalyzerException(insn, "Third argument", expected3,
                    value3);
        }
        return null;
    }

    @Override
    public TypeValue naryOperation(final AbstractInsnNode insn,
            final List<? extends TypeValue> values) throws AnalyzerException {
        int opcode = insn.getOpcode();
        if (opcode == MULTIANEWARRAY) {
            for (int i = 0; i < values.size(); ++i) {
                if (!TypeValue.INT_VALUE.equals(values.get(i))) {
                    throw new AnalyzerException(insn, null,
                            TypeValue.INT_VALUE, values.get(i));
                }
            }
        } else {
            int i = 0;
            int j = 0;
            if (opcode != INVOKESTATIC && opcode != INVOKEDYNAMIC) {
                Type owner = Type.getObjectType(((MethodInsnNode) insn).owner);
                if (!isSubTypeOf(values.get(i++), newValue(owner))) {
                    throw new AnalyzerException(insn, "Method owner",
                            newValue(owner), values.get(0));
                }
            }
            String desc = (opcode == INVOKEDYNAMIC) ? ((InvokeDynamicInsnNode) insn).desc
                    : ((MethodInsnNode) insn).desc;
            Type[] args = Type.getArgumentTypes(desc);
            while (i < values.size()) {
                TypeValue expected = newValue(args[j++]);
                TypeValue encountered = values.get(i++);
                if (!isSubTypeOf(encountered, expected)) {
                    throw new AnalyzerException(insn, "Argument " + j,
                            expected, encountered);
                }
            }
        }
        return super.naryOperation(insn, values);
    }

    @Override
    public void returnOperation(final AbstractInsnNode insn,
            final TypeValue value, final TypeValue expected)
            throws AnalyzerException {
        if (!isSubTypeOf(value, expected)) {
            throw new AnalyzerException(insn, "Incompatible return type",
                    expected, value);
        }
    }

    protected boolean isArrayValue(final TypeValue value) {
        return value.isReference();
    }

    protected TypeValue getElementValue(final TypeValue objectArrayValue)
            throws AnalyzerException {
        return TypeValue.REFERENCE_VALUE;
    }

    protected boolean isSubTypeOf(final TypeValue value,
            final TypeValue expected) {
        return value.equals(expected);
    }
}
