package samples;

import jawaitasync.Promise;
import jawaitasync.tools.AsyncSocket;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class Test2Example {
	public Promise testAsync() throws Exception {
		await(handleSocket());
		return complete(null);
	}

	public Promise handleSocket() throws UnsupportedEncodingException {
		return complete(new String(await(Promise.resolved(new byte[10])), "UTF-8"));
	}
}
