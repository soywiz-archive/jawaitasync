import com.sun.xml.internal.ws.encoding.DataHandlerDataSource;
import jawaitasync.Promise;
import jawaitasync.PromiseTools;
import jawaitasync.ResultRunnable;
import jawaitasync.tools.AsyncSocket;
import jawaitasync.tools.AsyncSocketListener;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

import static jawaitasync.Promise.await;

public class Start {
	static public void start() throws Exception {
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

	static public void handleSocket(AsyncSocket socket) throws UnsupportedEncodingException {
		//String line = new String(await(socket.readUntilAsync(new byte[] { '\r', '\n' })), "UTF-8");
		byte[] data = await(socket.readBytesAsync(64));
		//socket.close();
		System.out.println("readed: " + new String(data, "UTF-8"));
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
