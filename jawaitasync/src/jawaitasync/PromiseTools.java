package jawaitasync;

public class PromiseTools {
	static public Promise<String> downloadUrl(String url) {
		//return 0;
		return null;
	}

	static public Promise sleep(int milliseconds) {
		Promise promise = new Promise();
		EventLoop.setTimeout(promise::resolve, milliseconds);
		return promise;
	}
}
