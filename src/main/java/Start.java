import jawaitasync.Promise;
import jawaitasync.tools.AsyncSocket;
import jawaitasync.tools.AsyncSocketListener;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class Start {
	public void start() throws Exception {
		//new CompositionExample().testAsync();
		/*
		Promise<Integer> p = longTask();
		Promise<String> p2 = new DownloadUrlExample().downloadFilesAsync();
		int result2 = (int)await(p);
		String result = await(p2);
		System.out.println(result2);
		System.out.println(result);
		*/
		AsyncSocketListener asl = new AsyncSocketListener();
		System.out.println("Started");
		await(asl.bindAsync(new InetSocketAddress("127.0.0.1", 8081)));
		while (true) {
			System.out.println("listening at 8081");
			AsyncSocket socket = await(asl.acceptAsync());
			System.out.println("accepted: " + socket);
			handleSocket(socket);
		}
	}

	public Promise<byte[]> readUntilAsync(AsyncSocket socket, byte c) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] readed;
		do {
			readed = await(socket.readBytesAsync(1));
			baos.write(readed[0]);
		} while(readed[0] != c);
		return complete(baos.toByteArray());
	}

	public void handleSocket(AsyncSocket socket) throws UnsupportedEncodingException {
		byte[] bytes = await(readUntilAsync(socket, (byte)'\n'));
		String line = new String(bytes, "UTF-8");
		//byte[] data = await(socket.readBytesAsync(64));
		//socket.close();
		System.out.println("readed: " + line);
	}

/*
	static public Promise<Integer> longTask() throws Exception {
		return PromiseTools.runTaskAsync(() -> {
			long m = 0;
			for (long n = 0; n < 2000000000L; n++) m += n;
			return (int)m;
		});
	}
*/
}
