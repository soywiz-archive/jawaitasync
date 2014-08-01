package jawaitasync.processor;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Linq<T> implements Iterable<T> {
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

	public Linq<T> skip(int count) {
		List<T> out = new LinkedList<>();
		for (T item : list) {
			if (count <= 0) {
				out.add(item);
			} else {
				count--;
			}
		}
		return new Linq(out);
	}

	static public int[] range(int count) {
		return range(0, count);
	}

	static public int[] range(int start, int count) {
		int[] result = new int[count];
		for (int n = 0; n < count; n++) result[n] = n + start;
		return result;
	}

	public <T> List<T> toList(Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		for (Object item : this) list.add((T)item);
		return list;
	}

	public <T> T[] toArray(Class<T> clazz) {
		List<T> list = toList(clazz);
		Object array = Array.newInstance(clazz, list.size());
		for (int n = 0; n < list.size(); n++) Array.set(array, n, list.get(n));
		return (T[])array;
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