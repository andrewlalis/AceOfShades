package nl.andrewlalis.aos_core.net.chat;

import nl.andrewlalis.aos_core.net.Message;
import nl.andrewlalis.aos_core.net.Type;

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
