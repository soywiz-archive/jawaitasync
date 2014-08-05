package samples;

import static jawaitasync.Promise.await;
import static jawaitasync.PromiseTools.sleepAsync;

public class AccessPrivateMethodExample {
	public String publicField = "public";

	public void testAsync() {
		System.out.print(publicField);
		await(sleepAsync(1000));
		System.out.print(privateMethod());
	}

	private String privateMethod() {
		return "privateMethod";
	}
}
