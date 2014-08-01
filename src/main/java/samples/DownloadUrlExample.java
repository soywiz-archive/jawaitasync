package samples;

import jawaitasync.Promise;
import jawaitasync.PromiseTools;

import java.io.IOException;

import static jawaitasync.Promise.await;
import static jawaitasync.Promise.complete;
import static jawaitasync.PromiseTools.sleep;

public class DownloadUrlExample {
	public Promise downloadFilesAsync() throws IOException {
		String file = await(PromiseTools.downloadUrl("http://google.es/"));
		System.out.println(file);
		await(sleep(1000));
		String file2 = await(PromiseTools.downloadUrl("http://www.google.es/"));
		System.out.println(file2);
		return complete(null);
	}
}
