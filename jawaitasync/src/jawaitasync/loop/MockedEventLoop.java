package jawaitasync.loop;

public class MockedEventLoop extends NormalEventLoop {
	long currentTime = 0;

	@Override
	protected long getNow() {
		return currentTime;
	}

	@Override
	protected void sleep(long timeToWait) throws Exception {
		System.out.print("[" + currentTime + ":" + timeToWait + "]");
		currentTime += timeToWait;
	}
}