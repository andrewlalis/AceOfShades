package nl.andrewlalis.aos_core.net;

import nl.andrewlalis.aos_core.model.PlayerControlState;

public class PlayerControlStateMessage extends Message {
	private final PlayerControlState playerControlState;

	public PlayerControlStateMessage(PlayerControlState pcs) {
		super(Type.PLAYER_CONTROL_STATE);
		this.playerControlState = pcs;
	}

	public PlayerControlState getPlayerControlState() {
		return playerControlState;
	}
}
