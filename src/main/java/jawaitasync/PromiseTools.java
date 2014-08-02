package jawaitasync;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import jawaitasync.loop.EventLoopHolder;

import java.io.IOException;

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
		Promise<?> promise = new Promise();
		EventLoopHolder.instance.setTimeout(() -> {
			promise.resolve(null);
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
}
