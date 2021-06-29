package nl.andrewlalis.aos_core.net.chat;

import nl.andrewlalis.aos_core.net.Message;
import nl.andrewlalis.aos_core.net.Type;

public class ChatMessage extends Message {
	private final String text;
	private final ChatType chatType;

	public ChatMessage(String text, ChatType chatType) {
		super(Type.CHAT);
		this.text = text;
		this.chatType = chatType;
	}

	public String getText() {
		return text;
	}

	public ChatType getChatType() {
		return chatType;
	}
}
