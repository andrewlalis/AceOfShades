package nl.andrewlalis.aos_core.net;

import nl.andrewlalis.aos_core.model.Player;

public class PlayerUpdateMessage extends Message {
	private final Player player;

	public PlayerUpdateMessage(Type type, Player player) {
		super(type);
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}
}
