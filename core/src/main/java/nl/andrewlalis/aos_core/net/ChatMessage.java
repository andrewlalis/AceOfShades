package nl.andrewlalis.aos_core.net;

public class ChatMessage extends Message {
	private final String text;

	public ChatMessage(String text) {
		super(Type.CHAT);
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
