package jawaitasync.processor;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;
import jawaitasync.processor.analyzer.TypeInterpreter;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class AwaitAnalyzer {
	public HashMap<AbstractInsnNode, AnalyzedFrame> framesByInstruction = new HashMap<>();
	public ClassNode outerClass;
	public MethodNode method;
	public InsnList instructions;

	public AwaitAnalyzer(ClassNode outerClass, MethodNode method) throws AnalyzerException {
		this.outerClass = outerClass;
		this.method = method;
		this.instructions = method.instructions;
		analyze();
	}

	private void analyze() throws AnalyzerException {
		Analyzer analyzer = new Analyzer(new TypeInterpreter());
		Frame[] frames = analyzer.analyze(outerClass.name, method);

		//AnalyzedFrame paf = null;
		int instructionsLength = instructions.size();

		int maxLocals = method.maxLocals;

		AnalyzedFrame[] analyzedFrames = new AnalyzedFrame[instructionsLength];

		for (int n = 0; n < instructionsLength; n++) {
			AbstractInsnNode instruction = instructions.get(n);
			Frame frame = frames[n];
			AnalyzedFrame af = new AnalyzedFrame();

			af.instruction = instruction;
			af.locals = new LocalVariableNode[maxLocals];
			af.clazz = outerClass;
			af.method = method;
			af.originalIndex = n;
			af.frame = frame;

			analyzedFrames[n] = af;
			framesByInstruction.put(instruction, af);
			//paf = af;
		}

		for (int n = 0; n < instructionsLength; n++) {
			AnalyzedFrame current = analyzedFrames[n];
			current.previous = (n > 0) ? analyzedFrames[n - 1] : current;
			current.next = (n < instructionsLength -1) ? analyzedFrames[n + 1] : current;
		}

		for (int m = 0; m < method.localVariables.size(); m++) {
			LocalVariableNode local = (LocalVariableNode)method.localVariables.get(m);
			int startIndex = method.instructions.indexOf(local.start);
			int endIndex = method.instructions.indexOf(local.end);

			//System.out.println(local.index + ";" + local.name + ";" + local.desc + ";" + startIndex + ";" + endIndex + ";" + instructionsLength);
			for (int index = startIndex; index <= endIndex; index++) {
				analyzedFrames[index].locals[local.index] = local;
			}
		}
	}

	public AnalyzedFrame[] prepare(InsnList instructions) {
		int instructionsLength = instructions.size();
		AnalyzedFrame[] afs = new AnalyzedFrame[instructionsLength];
		AnalyzedFrame paf = new AnalyzedFrame();
		paf.frame = new Frame(0, 0);
		paf.clazz = this.outerClass;
		paf.method = this.method;
		AnalyzedFrame af;
		for (int n = 0; n < instructionsLength; n++, paf = af) {
			AnalyzedFrame af2 = framesByInstruction.get(instructions.get(n));
			if (af2 != null) {
				af = af2;
			} else {
				af = new AnalyzedFrame();
				af.instruction = instructions.get(n);
				af.originalIndex = -1;
				af.frame = paf.frame;
				af.clazz = paf.clazz;
				af.next = paf.next;
				af.previous = paf.previous;
				af.locals = paf.locals;
				af.method = paf.method;
			}
			af.newIndex = n;
			afs[n] = af;
			/*
			System.out.print("af:" + n + ":[" + ClassNodeUtils.toString(af.instruction) + "]:" + af.originalIndex + "->" + af.newIndex + ":");
			for (int m = 0; m < af.locals.length; m++) {
				System.out.print((af.locals[m] != null) ? af.locals[m].name : "null");
				System.out.print(",");
			}
			System.out.println();
			*/
		}
		return afs;
	}
}
