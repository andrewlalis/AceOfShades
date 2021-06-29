package nl.andrewlalis.aos_core.net;

public class ConnectionRejectedMessage extends Message {
	private final String message;

	public ConnectionRejectedMessage(String message) {
		super(Type.CONNECTION_REJECTED);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
