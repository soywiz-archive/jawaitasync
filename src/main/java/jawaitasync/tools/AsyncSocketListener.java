package jawaitasync.tools;

import jawaitasync.Promise;
import jawaitasync.loop.EventLoopHolder;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.LinkedList;
import java.util.Queue;

public class AsyncSocketListener {
	public Promise<AsyncSocketListener> bindAsync(SocketAddress socketAddress) {
		Promise promise = new Promise();
		EventLoopHolder.instance.refCountInc();
		new Thread(() -> {
			try {
				try (Selector selector = Selector.open()) {
					SelectorProvider selectorProvider = selector.provider();
					ServerSocketChannel ssc = selectorProvider.openServerSocketChannel();
					try {
						ssc.bind(socketAddress, 20);
						ssc.configureBlocking(false);
						ssc.register(selector, SelectionKey.OP_ACCEPT);
						promise.resolve(this);
					} catch (Exception e) {
						e.printStackTrace();
						promise.reject(e);
						throw (e);
					}

					while (true) {
						//System.out.println("select0");
						int readyChannels = selector.select();
						//System.out.println("select: " + readyChannels);
						if (readyChannels == 0) continue;

						for (SelectionKey key : selector.selectedKeys()) {
							try {
								//System.out.println(key.interestOps() + ":" + key.channel());
								if (key.isAcceptable()) {
									ServerSocketChannel ssc2 = (ServerSocketChannel) key.channel();
									SocketChannel sc = ssc2.accept();
									sc.configureBlocking(false);
									sc.register(selector, SelectionKey.OP_READ);
									AsyncSocket as = new AsyncSocket(sc);
									sc.keyFor(selector).attach(as);
									onSocket(as);
									//System.out.println("accepted1");
								} else if (key.isReadable()) {
									AsyncSocket as = (AsyncSocket) key.attachment();
									ByteBuffer bb = ByteBuffer.allocate(8 * 1024);
									int len = as.socketChannel.read(bb);
									if (len < 0) {
										// Disconnected!
										as.onClose();
									} else {
										bb.flip();
										//System.out.println("remaining:" + bb.remaining());

										as.onData(bb);
									}
									//System.out.println(as);
								}
							} catch (Exception e) {
								//e.printStackTrace();
								key.cancel();
							}
						}
						selector.selectedKeys().clear();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				EventLoopHolder.instance.refCountDec();
			}
		}).start();
		return promise;
	}

	Queue<AsyncSocket> asList = new LinkedList<>();
	Queue<Promise<AsyncSocket>> promiseList = new LinkedList<>();

	private void onSocket(AsyncSocket as) {
		asList.add(as);
		tryCouple();
	}

	public Promise<AsyncSocket> acceptAsync() {
		Promise<AsyncSocket> promise = new Promise<>();
		promiseList.add(promise);
		tryCouple();
		return promise;
	}

	private void tryCouple() {
		while (asList.size() > 0 && promiseList.size() > 0) {
			AsyncSocket as = asList.poll();
			Promise<AsyncSocket> promise = promiseList.poll();
			//System.out.println("resolved!");
			promise.resolve(as);
		}
	}
}
