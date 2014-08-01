import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import jawaitasync.ResultRunnable;

import static jawaitasync.Promise.await;

public class PromiseExampleConverted {
	public Promise testAsync() {
		System.out.println("hello!");
		await(PromiseTools.sleep(1000));
		System.out.println("world!");
		return Promise.complete(null);
		/*
		int n = 0;
        System.out.println("hello!" + n++);
        await(PromiseTools.sleep(1000));
        System.out.println("world!" + n++);
        await(PromiseTools.sleep(1000));
        System.out.println("end!" + n++);
        return Promise.complete(null);
        */
	}
}

public class PromiseExampleConverted_testAsync_Runnable implements ResultRunnable {
	public int state = 0;
	public Promise promise = new Promise();
	public PromiseExample local_this;

	public PromiseExampleConverted_testAsync_Runnable(PromiseExample paramPromiseExample)
	{
		this.local_this = paramPromiseExample;
	}

	public void run(Object paramObject)
	{
		switch (this.state)
		{
			case 0:
			default:
				System.out.println("hello!");
				PromiseTools.sleep(1000).then(this);this.state = 1;return;
			case 1:
				System.out.println("world!");
				promise.resolve(null);
		}
	}
}
