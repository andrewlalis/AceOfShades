package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_core.net.chat.ChatMessage;
import nl.andrewlalis.aos_core.net.chat.PlayerChatMessage;
import nl.andrewlalis.aos_core.net.data.Sound;
import nl.andrewlalis.aos_core.net.data.SoundType;

import java.util.LinkedList;
import java.util.List;

public class ChatManager {
	public static final int MAX_CHAT_MESSAGES = 10;

	private final List<ChatMessage> chatMessages;
	private boolean chatting = false;
	private final StringBuilder chatBuffer;

	private final SoundManager soundManager;
	private MessageTransceiver messageTransceiver;

	public ChatManager(SoundManager soundManager) {
		this.soundManager = soundManager;
		this.chatMessages = new LinkedList<>();
		this.chatBuffer = new StringBuilder();
	}

	public void bindTransceiver(MessageTransceiver messageTransceiver) {
		this.messageTransceiver = messageTransceiver;
	}

	public void unbindTransceiver() {
		this.messageTransceiver = null;
	}

	public synchronized void addChatMessage(ChatMessage message) {
		this.chatMessages.add(message);
		if (message.getClass() == PlayerChatMessage.class) {
			this.soundManager.play(new Sound(null, 1.0f, SoundType.CHAT));
		}
		while (this.chatMessages.size() > MAX_CHAT_MESSAGES) {
			this.chatMessages.remove(0);
		}
	}

	public ChatMessage[] getLatestChatMessages() {
		return this.chatMessages.toArray(new ChatMessage[0]);
	}

	public boolean isChatting() {
		return this.chatting;
	}

	public void setChatting(boolean chatting) {
		this.chatting = chatting;
		if (this.chatting) {
			this.chatBuffer.setLength(0);
		}
	}

	public void appendToChat(char c) {
		this.chatBuffer.append(c);
	}

	public void backspaceChat() {
		if (this.chatBuffer.length() > 0) {
			this.chatBuffer.setLength(this.chatBuffer.length() - 1);
		}
	}

	public void sendChat() {
		String message = this.chatBuffer.toString().trim();
		if (!message.isBlank() && !message.equals("/") && this.messageTransceiver != null) {
			this.messageTransceiver.send(new ChatMessage(message));
		}
		this.setChatting(false);
	}

	public String getCurrentChatBuffer() {
		return this.chatBuffer.toString();
	}
}
