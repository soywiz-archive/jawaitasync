jawaitasync
===========

[![Build Status](https://travis-ci.org/soywiz/jawaitasync.svg?branch=master)](https://travis-ci.org/soywiz/jawaitasync)

Implements C# await/async keywords and behaviour with any JVM language. Completely asynchronous.

```java
public class DownloadUrlExample {
	public Promise<String> downloadFilesAsync() throws IOException {
		String file = await(PromiseTools.downloadUrl("http://google.es/"));
		System.out.println(file);
		await(sleep(1000));
		String file2 = await(PromiseTools.downloadUrl("http://www.google.es/"));
		System.out.println(file2);
		return complete(file2);
	}
}
```

How this works?

This behaviour is implemented post-processing class files. It analyzes class files trying to find Promise.await calls. When it found a method calling await, it start reconstructing that method. Converts the method into a new class containing all the local variables as fields, and converts the function into a machine state. Each await call, suspends the function, and when the promise is done, it resumes the function. All this work is completely transparent. You just write the above code and it works, run the class postprocessor jawaitasync (usually automatically), and run the code. Also it allows to use a custom loader, so the modifications are made on the fly at runtime without any postprocessing, just using a custom ClassLoader.
