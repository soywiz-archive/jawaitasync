package jawaitasync;

import jawaitasync.loop.EventLoop;
import jawaitasync.loop.EventLoopHolder;

public class PromiseTools {
	static public Promise<String> downloadUrl(String url) {
		//return 0;
		return null;
	}

	static public Promise sleep(int milliseconds) {
		Promise promise = new Promise();
		EventLoopHolder.instance.setTimeout(promise::resolve, milliseconds);
		return promise;
	}
}
