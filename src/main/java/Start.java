import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import samples.DownloadUrlExample;

import static jawaitasync.Promise.await;

public class Start {
	static public void start() throws Exception {
		//new CompositionExample().testAsync();
		Promise<Integer> p = longTask();
		Promise<String> p2 = new DownloadUrlExample().downloadFilesAsync();
		int result2 = (int)await(p);
		String result = await(p2);
		System.out.println(result2);
		System.out.println(result);
	}

	static public Promise<Integer> longTask() throws Exception {
		return PromiseTools.runTaskAsync(() -> {
			long m = 0;
			for (long n = 0; n < 2000000000L; n++) m += n;
			return (int)m;
		});
	}
}
