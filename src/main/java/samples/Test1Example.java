package samples;

import jawaitasync.Promise;
import jawaitasync.tools.AsyncSocket;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class Test1Example {
	public Promise<byte[]> testAsync() {
		return complete(await(readUntilAsync(null, (byte) 0)));
	}

	public Promise<byte[]> readUntilAsync(AsyncSocket socket, byte c) {
		return complete(await(readBytesAsync(1)));
	}

	public Promise<byte[]> readBytesAsync(int count) {
		return Promise.resolved(new byte[count]);
	}
}
