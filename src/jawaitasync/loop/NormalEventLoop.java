package jawaitasync.loop;

import jawaitasync.ResultRunnable;

import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;

public class NormalEventLoop implements EventLoop {
	private TreeSet<Timer> timers = new TreeSet<Timer>(new HolderTimeComparator());

	protected long getNow() {
		return new Date().getTime();
	}

	public void setTimeout(ResultRunnable r, int time) {
		timers.add(new Timer(getNow() + time, r));
	}

	public void loop() throws Exception {
		while (!timers.isEmpty()) {
			Timer timer = timers.pollFirst();
			long timeToWait = timer.time - getNow();
			if (timeToWait > 0) sleep(timeToWait);
			timer.run.run(null);
		}
	}

	protected void sleep(long timeToWait) throws Exception {
		Thread.sleep(timeToWait);
	}
}

class HolderTimeComparator implements Comparator<Timer> {
	@Override
	public int compare(Timer a, Timer b) {
		if (a.time < b.time) return -1;
		if (a.time > b.time) return +1;
		return 0;
	}
}

class Timer {
	public long time;
	public ResultRunnable run;

	Timer(long time, ResultRunnable run) {
		this.time = time;
		this.run = run;
	}
}