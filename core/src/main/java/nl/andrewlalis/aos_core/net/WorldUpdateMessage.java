package nl.andrewlalis.aos_core.net;

import nl.andrewlalis.aos_core.model.World;

public class WorldUpdateMessage extends Message {
	private final World world;
	public WorldUpdateMessage(World world) {
		super(Type.WORLD_UPDATE);
		this.world = world;
	}

	public World getWorld() {
		return world;
	}
}
