package samples;

import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;

public class Test3Example {
	public void testAsync() {
		await(PromiseTools.sleepAsync(1000));
		int m = 0;
		for (int n = 0; n < 20; n++) m += n;
		byte[] test = new byte[20];
		for (int n = 0; n < 20; n++) test[n] = (byte)(n + 100);
		await(PromiseTools.sleepAsync(1000));
		System.out.println(test[7]);
	}
}
