package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import java.io.IOException;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;
import static jawaitasync.PromiseTools.downloadUrlAsync;
import static jawaitasync.PromiseTools.sleepAsync;

public class DownloadUrlExample {
	public Promise downloadFilesAsync() throws IOException {
		String file = await(downloadUrlAsync("http://google.es/"));
		System.out.println(file);
		await(sleepAsync(1000));
		String file2 = await(downloadUrlAsync("http://www.google.es/"));
		System.out.println(file2);
		return complete(null);
	}
}
