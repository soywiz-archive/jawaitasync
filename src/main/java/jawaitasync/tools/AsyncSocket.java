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
	ByteBuffer bb;
	LinkedList<ByteBuffer> bbs = new LinkedList<>();
	Queue<ReadRequest> requests = new LinkedList<>();

	void onData(ByteBuffer bb) {
		totalAvailable += bb.remaining();
		bbs.add(bb);
		tryCouple();
	}

	byte readByte() {
		if (bb == null || bb.remaining() <= 0) bb = bbs.poll();
		totalAvailable--;
		return (bb != null) ? bb.get() : 0;
	}

	public AsyncSocket write(byte[] bytes) throws IOException {
		socketChannel.write(ByteBuffer.wrap(bytes));
		return this;
	}

	void tryCouple() {
		while (requests.size() > 0) {
			ReadRequest request = requests.peek();
			if (request.chunk(this)) {
				requests.poll();
			} else {
				break;
			}
		}
	}

	public Promise<byte[]> readBytesAsync(int count) {
		Promise<byte[]> promise = new Promise<>();
		requests.add(new ReadRequestFixed(new byte[count], promise));
		tryCouple();
		return promise;
	}

	public Promise<byte[]> readUntilAsync(byte c) {
		Promise<byte[]> promise = new Promise<>();
		requests.add(new ReadRequestEndByte(c, promise));
		tryCouple();
		return promise;
	}

	public void close() throws IOException {
		socketChannel.close();
	}
}

interface ReadRequest {
	boolean chunk(AsyncSocket socket);
}

class ReadRequestFixed implements ReadRequest {
	public byte[] buffer;
	public int index;
	public Promise<byte[]> promise;

	ReadRequestFixed(byte[] buffer, Promise<byte[]> promise) {
		this.buffer = buffer;
		this.index = 0;
		this.promise = promise;
	}

	public boolean chunk(AsyncSocket socket) {
		while (this.index < buffer.length) {
			if (socket.totalAvailable <= 0) return false;
			this.buffer[this.index++] = socket.readByte();
		}
		promise.resolve(this.buffer);
		return true;
	}
}


class ReadRequestEndByte implements ReadRequest {
	public ByteArrayOutputStream baos;
	public byte endByte;
	public Promise<byte[]> promise;

	ReadRequestEndByte(byte endByte, Promise<byte[]> promise) {
		this.baos = new ByteArrayOutputStream();
		this.endByte = endByte;
		this.promise = promise;
	}

	public boolean chunk(AsyncSocket socket) {
		while (socket.totalAvailable > 0) {
			byte readByte = socket.readByte();
			baos.write(readByte);
			if (readByte == endByte) {
				promise.resolve(baos.toByteArray());
				return true;
			}
		}
		/*
		byte[] data2 = baos.toByteArray();
		for (int n = 0; n < data2.length; n++) {
			System.out.println(":" + data2[n] + " - " + (char)data2[n]);
		}
		System.out.println("need more data! : " + baos.size() + " : " + endByte);
		*/
		return false;
	}
}