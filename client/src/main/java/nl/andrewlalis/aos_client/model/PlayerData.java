package nl.andrewlalis.aos_client.model;

import nl.andrewlalis.aos_core.geom.Vec2;

/**
 * The data about a player which the client needs to know in order to render it.
 */
public class PlayerData {
	private int id;
	private String name;
	private byte teamId;

	private Vec2 position;
	private Vec2 orientation;
	private Vec2 velocity;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public byte getTeamId() {
		return teamId;
	}

	public Vec2 getPosition() {
		return position;
	}

	public Vec2 getOrientation() {
		return orientation;
	}

	public Vec2 getVelocity() {
		return velocity;
	}
}
