import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import static jawaitasync.Promise.await;

public class PromiseExample {
    public Promise testAsync() {
        System.out.println("hello!");
        await(PromiseTools.sleep(1000));
        System.out.println("world!");
        await(PromiseTools.sleep(1000));
        System.out.println("end!");
        return Promise.complete(null);
    }
}
