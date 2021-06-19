package nl.andrewlalis.aos_core.net;

public class PlayerRegisteredMessage extends Message {
	private final int playerId;

	public PlayerRegisteredMessage(int playerId) {
		super(Type.PLAYER_REGISTERED);
		this.playerId = playerId;
	}

	public int getPlayerId() {
		return playerId;
	}
}
