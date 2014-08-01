package jawaitasync;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import jawaitasync.loop.EventLoopHolder;

import java.io.IOException;

public class PromiseTools {
	static public Promise<String> downloadUrl(String url) throws IOException {
		Promise promise = new Promise();

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
				promise.resolve(t);
			}
		});

		return promise;
	}

	static public Promise sleep(int milliseconds) {
		Promise promise = new Promise();
		EventLoopHolder.instance.setTimeout(() -> {
			promise.resolve(null);
		}, milliseconds);
		return promise;
	}
}
