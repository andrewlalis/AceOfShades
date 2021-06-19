package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;

import java.io.Serializable;

public class PlayerControlState implements Serializable {
	private int playerId;

	boolean movingLeft;
	boolean movingRight;
	boolean movingForward;
	boolean movingBackward;

	boolean shooting;
	boolean reloading;

	Vec2 mouseLocation;

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public boolean isMovingLeft() {
		return movingLeft;
	}

	public void setMovingLeft(boolean movingLeft) {
		this.movingLeft = movingLeft;
	}

	public boolean isMovingRight() {
		return movingRight;
	}

	public void setMovingRight(boolean movingRight) {
		this.movingRight = movingRight;
	}

	public boolean isMovingForward() {
		return movingForward;
	}

	public void setMovingForward(boolean movingForward) {
		this.movingForward = movingForward;
	}

	public boolean isMovingBackward() {
		return movingBackward;
	}

	public void setMovingBackward(boolean movingBackward) {
		this.movingBackward = movingBackward;
	}

	public boolean isShooting() {
		return shooting;
	}

	public void setShooting(boolean shooting) {
		this.shooting = shooting;
	}

	public boolean isReloading() {
		return reloading;
	}

	public void setReloading(boolean reloading) {
		this.reloading = reloading;
	}

	public Vec2 getMouseLocation() {
		return mouseLocation;
	}

	public void setMouseLocation(Vec2 mouseLocation) {
		this.mouseLocation = mouseLocation;
	}
}
