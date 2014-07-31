import jawaitasync.Async;
import jawaitasync.Promise;
import jawaitasync.PromiseTools;

public class TestMain {
	@Async
	static public Promise<Integer> test() {
		for (int n = 0; n < 2; n++) {
			String html = Promise.await(PromiseTools.downloadUrl("http://www.google.es/"));
			System.out.println(html);
			Promise.await(PromiseTools.sleep(1000));
		}

		return Promise.complete(0);
	}

	public static void main(String[] args) {
		System.out.println("Hello world!");
	}
}
