package jawaitasync.processor;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;

import java.util.*;

public class JumpTree {
	public LabelNode labelNode;
	public SortedSet<Integer> keys = new TreeSet<>();
	public HashMap<Integer, Object> map = new HashMap<>();

	public JumpTree(LabelNode labelNode) {
		this.labelNode = labelNode;
	}

	void addLabel(int key, LabelNode label) {
		keys.add(key);
		map.put(key, label);
	}

	void addSubtree(int key, JumpTree subtree) {
		keys.add(key);
		map.put(key, subtree);
	}

	LabelNode getKeyLabel(int key) {
		Object object = map.get(key);
		if (object instanceof JumpTree) return ((JumpTree)object).labelNode;
		return (LabelNode)object;
	}

	InsnList createList() {
		InsnList list = new InsnList();

		int[] keys2 = new int[keys.size()];
		LabelNode[] labels = new LabelNode[keys.size()];

		int index = 0;
		for (Integer key : keys) {
			keys2[index] = key;
			labels[index] = getKeyLabel(key);
			index++;
		}

		list.add(new LookupSwitchInsnNode(labels[0], keys2, labels));
		return list;
	}
}
