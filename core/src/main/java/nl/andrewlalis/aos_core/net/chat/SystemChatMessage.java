package nl.andrewlalis.aos_core.net.chat;

public class SystemChatMessage extends ChatMessage {
	public enum Level {INFO, WARNING, SEVERE}

	private final Level level;

	public SystemChatMessage(Level level, String text) {
		super(text, ChatType.SYSTEM_MESSAGE);
		this.level = level;
	}

	public Level getLevel() {
		return level;
	}
}
