package jawaitasync;

import jawaitasync.loop.EventLoopHolder;
import sun.net.www.content.text.Generic;

import java.util.LinkedList;
import java.util.Queue;

public class Promise<T> {
	Queue<ResultRunnable> callbacks = new LinkedList<>();
	boolean resolved = false;
	Object resolvedValue;

	public void then(ResultRunnable<T> callback) {
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

	public void reject(Exception exception) {
		resolved = true;
		resolvedValue = exception;
		checkResolved();
	}

	private void checkResolved() {
		if (!resolved) return;
		while (callbacks.peek() != null) {
			final ResultRunnable callback = callbacks.poll();
			EventLoopHolder.instance.enqueue(() -> {
				callback.run(resolvedValue);
			});
		}
	}

	static native public <T> T await(Promise<T> promise);

	static native public <T> Promise<T> complete(T promise);
}
