public class RequestHeader {
	private String key;
	private String value;

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public RequestHeader(String key, String value) {
		this.key = key;

		this.value = value;
	}
}
