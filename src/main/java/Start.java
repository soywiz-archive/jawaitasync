import jawaitasync.Promise;
import jawaitasync.tools.AsyncSocket;
import jawaitasync.tools.AsyncSocketListener;

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
		System.out.println("listening at 8081");
		while (true) {
			AsyncSocket socket = await(asl.acceptAsync());
			//System.out.println("accepted: " + socket);
			handleSocket(socket);
		}
	}

	public Promise<String> readLineAsync(AsyncSocket socket) throws UnsupportedEncodingException {
		byte[] bytes = await(socket.readUntilAsync((byte)'\n'));
		return complete(new String(bytes, "UTF-8"));
	}

	private Promise<Request> readHeadersAsync(AsyncSocket socket) throws UnsupportedEncodingException {
		String line;

		Request request = new Request();
		line = await(readLineAsync(socket)).trim();
		String[] parts = line.split(" ");
		request.method = parts[0];

		while (true) {
			line = await(readLineAsync(socket)).trim();
			if (line.length() == 0) break;
			String[] headerParts = line.split(":", 2);
			request.headers.add(new RequestHeader(headerParts[0].trim(), headerParts[1].trim()));
			//System.out.println("::" + line);
		}

		return complete(request);
	}

	public void handleSocket(AsyncSocket socket) throws Exception {
		try {
			Request request = await(readHeadersAsync(socket));

			String content = request.method + ": Hello World!\n";

			for (int n = 0; n < request.headers.size(); n++) {
				RequestHeader header = request.headers.get(n);
				//content += n + ": " + n + "<br />\n";
				content += header.getKey() + ": " + header.getValue() + "<br />\n";
			}

			// Not working!
			//for (RequestHeader header : request.headers) {
			//	content += header.getKey() + ": " + header.getValue() + "<br />\n";
			//}

			//byte[] contentBytes = content.getBytes();
			socket
				.write(("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: " + content.getBytes().length + "\r\n\r\n").getBytes())
				.write(content.getBytes())
			;
		} finally {
			socket.close();
		}
	}
}

