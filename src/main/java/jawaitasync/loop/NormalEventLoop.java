package jawaitasync.loop;

import java.util.*;

public class NormalEventLoop implements EventLoop {
	private Thread loopThread;
	private Queue<Runnable> callbacks = new LinkedList<>();
	Timer timer = new Timer();
	volatile long refcount = 0;

	synchronized public void refCountInc() { refcount++; }
	synchronized public void refCountDec() { refcount--; }

	public void setTimeout(final Runnable r, int time) {
		refCountInc();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				refCountDec();
				enqueue(r);
			}
		}, time);
	}

	@Override
	synchronized public void enqueue(Runnable r) {
		callbacks.add(r);
		notifyAll();
	}

	synchronized private Runnable readOne() throws InterruptedException {
		while (callbacks.size() == 0) wait();
		return callbacks.poll();
	}

	synchronized private boolean isEmpty() {
		return callbacks.isEmpty();
	}

	public void loop() throws Exception {
		loopThread = Thread.currentThread();
		while (!isEmpty() || (refcount > 0)) {
			Runnable runnable = readOne();
			if (runnable != null) runnable.run();
		}
		timer.cancel();
	}
}
