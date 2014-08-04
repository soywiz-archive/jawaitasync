package samples;

import static jawaitasync.Promise.await;
import static jawaitasync.PromiseTools.sleepAsync;

public class AccessPrivateExample {
	public String publicField = "public";
	private String privateField = "private";

	public void testAsync() {
		System.out.print(publicField);
		await(sleepAsync(1000));
		System.out.print(privateField);
		privateField = "changed";
		await(sleepAsync(1000));
		System.out.print(privateField);
	}
}
