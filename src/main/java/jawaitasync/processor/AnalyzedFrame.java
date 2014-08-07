package jawaitasync.processor;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;

public class AnalyzedFrame {
	public ClassNode clazz;
	public MethodNode method;
	public AnalyzedFrame previous;
	public AnalyzedFrame next;
	public Frame frame;
	public AbstractInsnNode instruction;
	public LocalVariableNode[] locals;
	public int originalIndex;
	public int newIndex;
}
