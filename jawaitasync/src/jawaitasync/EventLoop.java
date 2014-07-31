package jawaitasync;

import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;

public class EventLoop {
	static private TreeSet<Timer> timers = new TreeSet<Timer>(new HolderTimeComparator());

	public static long getNow() {
		return new Date().getTime();
	}

	public static void setTimeout(ResultRunnable r, int time) {
		timers.add(new Timer(getNow() + time, r));
	}

	public static void loop() throws Exception {
		while (!timers.isEmpty()) {
			Timer timer = timers.pollFirst();
			long timeToWait = timer.time - getNow();
			if (timeToWait > 0) Thread.sleep(timeToWait);
			timer.run.run(null);
		}
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