import java.util.LinkedList;
import java.util.List;

public class Request {
	public String method;
	public List<RequestHeader> headers = new LinkedList<>();
}
