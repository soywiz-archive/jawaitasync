package jawaitasync.processor;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.objectweb.asm.Opcodes.*;

public class AsmProcessor {
    public void test() throws Exception {
        ClassNode cn = new ClassNode();
        cn.version = V1_8;
        cn.access = ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE;
        cn.name = "pkg/Comparable";
        cn.superName = "java/lang/Object";
        cn.interfaces.add("pkg/Mesurable");
        cn.fields.add(new FieldNode(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "LESS", "I", null, new Integer(-1)));
        cn.fields.add(new FieldNode(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "EQUAL", "I", null, new Integer(0)));
        cn.fields.add(new FieldNode(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "GREATER", "I", null, new Integer(1)));
        cn.methods.add(new MethodNode(ACC_PUBLIC + ACC_ABSTRACT, "compareTo", "(Ljava/lang/Object;)I", null, null));
        MethodNode mn = new MethodNode(ACC_PUBLIC, "compareTo2", "(Ljava/lang/Object;)I", null, null);
        InsnList il = new InsnList();
        //il.add(new IntInsnNode(ILOAD_, 1));
        il.add(new InsnNode(ICONST_1));
        il.add(new InsnNode(IRETURN));
        mn.instructions.insert(il);
        cn.methods.add(mn);

        //ClassLoader cl = new ClassLoader();
        //new ClassReader()

        writeClass(new File("c:/temp/out.class"), cn);
        ClassNode cn2 = readClass(new File("c:/temp/out.class"));
        writeClass(new File("c:/temp/out2.class"), cn2);
    }

    private static ClassNode readClass(File file) throws IOException {
        ClassReader cr = new ClassReader(FileUtils.readFileToByteArray(file));
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
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
