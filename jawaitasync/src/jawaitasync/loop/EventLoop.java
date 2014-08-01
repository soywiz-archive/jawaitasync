package jawaitasync.loop;

import jawaitasync.ResultRunnable;

public interface EventLoop {
	void setTimeout(ResultRunnable r, int time);
	void loop() throws Exception;
}
