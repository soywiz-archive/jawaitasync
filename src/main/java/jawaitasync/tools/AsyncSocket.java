package jawaitasync.tools;

import jawaitasync.Promise;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;

public class AsyncSocket {
	SocketChannel socketChannel;

	public AsyncSocket(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	void onClose() {

	}

	int totalAvailable = 0;
	LinkedList<ByteBuffer> bbs = new LinkedList<>();
	Queue<ReadRequest> requests = new LinkedList<>();

	void onData(ByteBuffer bb) {
		totalAvailable += bb.remaining();
		//bb.array()
		bbs.add(bb);
		tryCouple();
	}

	byte readByte() {
		ByteBuffer bb = bbs.peek();
		while (bb != null && bb.remaining() <= 0) {
			bb = bbs.poll();
		}
		totalAvailable--;
		return bb.get();
	}

	void tryCouple() {
		while (requests.size() > 0 && totalAvailable >= requests.peek().size) {
			ReadRequest request = requests.poll();
			byte[] bytes = new byte[request.size];
			for (int n = 0; n < request.size; n++) bytes[n] = readByte();
			request.promise.resolve(bytes);
		}
	}

	/*
	public Promise<byte[]> readUntilAsync(byte[] sequence) {
		Promise<byte[]> promise = new Promise<>();
		requests.add(new ReadRequest(count, promise));
		tryCouple();
		return promise;
	}
	*/

	public Promise<byte[]> readAnyBytes() {
		Promise<byte[]> promise = new Promise<>();
		requests.add(new ReadRequest(-1, promise));
		tryCouple();
		return promise;
	}

	public Promise<byte[]> readBytesAsync(int count) {
		Promise<byte[]> promise = new Promise<>();
		requests.add(new ReadRequest(count, promise));
		tryCouple();
		return promise;
	}

	public void close() throws IOException {
		socketChannel.close();
	}
}

class ReadRequest {
	public int size;
	public Promise<byte[]> promise;

	ReadRequest(int size, Promise<byte[]> promise) {
		this.size = size;
		this.promise = promise;
	}
}