package nl.andrewlalis.aos_core.net;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.World;

public class PlayerRegisteredMessage extends Message {
	private final Player player;
	private final World world;

	public PlayerRegisteredMessage(Player player, World world) {
		super(Type.PLAYER_REGISTERED);
		this.player = player;
		this.world = world;
	}

	public Player getPlayer() {
		return player;
	}

	public World getWorld() {
		return world;
	}
}
