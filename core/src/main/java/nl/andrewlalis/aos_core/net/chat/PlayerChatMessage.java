package nl.andrewlalis.aos_core.net.chat;

public class PlayerChatMessage extends ChatMessage {
	private final int playerId;

	public PlayerChatMessage(int id, String text, ChatType chatType) {
		super(text, chatType);
		this.playerId = id;
	}

	public int getPlayerId() {
		return playerId;
	}
}
