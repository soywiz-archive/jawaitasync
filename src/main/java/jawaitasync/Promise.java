package jawaitasync;

import java.util.LinkedList;
import java.util.Queue;

public class Promise<T> {
	Queue<ResultRunnable> callbacks = new LinkedList<>();
	boolean resolved = false;
	T resolvedValue;

	public void then(ResultRunnable callback) {
		callbacks.add(callback);
		checkResolved();
	}

	public void then(Runnable callback) {
		callbacks.add((e) -> callback.run());
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
			ResultRunnable callback = callbacks.poll();
			callback.run(resolvedValue);
		}
	}

	static native public <T> T await(Promise<T> promise);

	static native public <T> Promise<T> complete(T promise);
}
