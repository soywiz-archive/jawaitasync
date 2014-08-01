package jawaitasync.loop;

import jawaitasync.ResultRunnable;

public interface EventLoop {
	void setTimeout(Runnable r, int time);
	void enqueue(Runnable r);
	void refCountInc();
	void refCountDec();
	void loop() throws Exception;
}
