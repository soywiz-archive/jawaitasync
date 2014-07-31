package jawaitasync;

public class Promise<T> {
	public void then() {
	}

	static native public <T> T await(Promise<T> promise);
	static native public <T> Promise<T> complete(T promise);
}
