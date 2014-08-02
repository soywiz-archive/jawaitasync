jawaitasync
===========

[![Build Status](https://travis-ci.org/soywiz/jawaitasync.svg?branch=master)](https://travis-ci.org/soywiz/jawaitasync)

Implements C# await/async keywords and behaviour with any JVM language. Completely asynchronous.

```java
public class DownloadUrlExample {
	public Promise<String> downloadFilesAsync() throws IOException {
		String file = await(PromiseTools.downloadUrlAsync("http://google.es/"));
		System.out.println(file);
		await(sleepAsync(1000));
		String file2 = await(PromiseTools.downloadUrlAsync("http://www.google.es/"));
		System.out.println(file2);
		return complete(file2);
	}
}
```

How does this works?

This behaviour is implemented post-processing class files.
The process analyzes methods in class files trying to find Promise.await calls.

When it found a method calling await, it start reconstructing that method.
Converts the method into a new class containing all the local variables as fields,
and converts the function into a machine state stored as a method in that class.

And then rewrites the original method so it calls new classes.
 
Each await call (that accept promises), suspends the function, and when the promise is done, the function is resumed.

All this work is completely transparent. You just write the above code and it works.

In order to get it working you have to:
* Run the class postprocessor jawaitasync (usually automatically), and run the code.
* Use a provided custom ClassLoader, so the modifications are made on the fly at runtime without any postprocessing just  when required.
