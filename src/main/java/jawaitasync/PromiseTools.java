package jawaitasync;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.client.websocket.WebSocketListener;
import jawaitasync.loop.EventLoopHolder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadPoolExecutor;

public class PromiseTools {
	static public Promise<String> downloadUrlAsync(String url) throws IOException {
		Promise<String> promise = new Promise();

		EventLoopHolder.instance.refCountInc();

		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<Response>() {

			@Override
			public Response onCompleted(Response response) throws Exception {
				EventLoopHolder.instance.refCountDec();
				promise.resolve(response.getResponseBody());
				return response;
			}

			@Override
			public void onThrowable(Throwable t) {
				EventLoopHolder.instance.refCountDec();
				promise.reject((Exception)t);
			}
		});

		return promise;
	}

	static public Promise sleepAsync(int milliseconds) {
		return sleepAsync(milliseconds, null);
	}

	static public <T> Promise<T> sleepAsync(int milliseconds, final T result) {
		Promise<T> promise = new Promise<>();
		EventLoopHolder.instance.setTimeout(() -> {
			promise.resolve(result);
		}, milliseconds);
		return promise;
	}

	static public Promise sleepAndThrowAsync(int milliseconds, Exception exception) {
		Promise<?> promise = new Promise();
		EventLoopHolder.instance.setTimeout(() -> {
			promise.reject(exception);
		}, milliseconds);
		return promise;
	}

	static public <T> Promise<T> runTaskAsync(RunnableYieldingResult<T> callback) {
		Promise<T> promise = new Promise<>();
		// @TODO: Use ThreadPool
		new Thread(() -> {
			promise.resolve(callback.run());
		}).start();
		return promise;
	}

	static public Promise<?> runTaskAsync(Runnable callback) {
		Promise<?> promise = new Promise<>();
		// @TODO: Use ThreadPool
		new Thread(() -> {
			callback.run();
			promise.resolve(null);
		}).start();
		return promise;
	}

	static public void test() throws Exception {
	}
}
