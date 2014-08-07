package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;

public class Test3Example {
	public void testAsync() {
		int m = 0;
		for (int n = 0; n < 20; n++) m += n;
		//byte[] test = new byte[m];
		String test = "test1";
		await(Promise.resolved(null));
		System.out.print(107);
	}
}
