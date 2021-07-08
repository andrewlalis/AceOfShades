package nl.andrewlalis.aos_server_registry.servlet;

public class ResponseStatusException extends Exception {
	private final int statusCode;
	private final String message;

	public ResponseStatusException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
		this.message = message;
	}

	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
