package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;

import java.io.Serializable;

/**
 * Base class for all objects that have basic movement physics.
 */
public abstract class PhysicsObject implements Serializable {
	private Vec2 position;
	private Vec2 orientation;
	private Vec2 velocity;

	public PhysicsObject(Vec2 position, Vec2 orientation, Vec2 velocity) {
		this.position = position;
		this.orientation = orientation;
		this.velocity = velocity;
	}

	public PhysicsObject() {
		this(new Vec2(0, 0), new Vec2(0, -1), new Vec2(0, 0));
	}

	public Vec2 getPosition() {
		return position;
	}

	public void setPosition(Vec2 position) {
		this.position = position;
	}

	public Vec2 getOrientation() {
		return orientation;
	}

	public void setOrientation(Vec2 orientation) {
		this.orientation = orientation;
	}

	public Vec2 getVelocity() {
		return velocity;
	}

	public void setVelocity(Vec2 velocity) {
		this.velocity = velocity;
	}
}
