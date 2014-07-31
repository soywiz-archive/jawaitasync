package jawaitasync;

import java.util.LinkedList;
import java.util.Queue;

public class Promise<T> {
	Queue<PromiseRunnable> callbacks = new LinkedList<>();
	boolean resolved = false;
	T resolvedValue;

	public void then(PromiseRunnable callback) {
		callbacks.add(callback);
		checkResolved();
	}

	public void resolve(T value) {
		resolved = true;
		resolvedValue = value;
		checkResolved();
	}

	private void checkResolved() {
		if (!resolved) return;
		while (callbacks.peek() != null) {
			PromiseRunnable callback = callbacks.poll();
			callback.run(resolvedValue);
		}
	}

	static native public <T> T await(Promise<T> promise);
	static native public <T> Promise<T> complete(T promise);
}
