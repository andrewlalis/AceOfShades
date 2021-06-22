package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.net.data.DataTypes;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class PlayerControlState implements Serializable {
	boolean movingLeft;
	boolean movingRight;
	boolean movingForward;
	boolean movingBackward;

	boolean shooting;
	boolean reloading;

	Vec2 mouseLocation;

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

	@Override
	public String toString() {
		return "PlayerControlState{" +
			"movingLeft=" + movingLeft +
			", movingRight=" + movingRight +
			", movingForward=" + movingForward +
			", movingBackward=" + movingBackward +
			", shooting=" + shooting +
			", reloading=" + reloading +
			", mouseLocation=" + mouseLocation +
			'}';
	}

	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + 2 * Float.BYTES);
		int flags = 0;
		if (this.movingLeft) flags |= 1;
		if (this.movingRight) flags |= 2;
		if (this.movingForward) flags |= 4;
		if (this.movingBackward) flags |= 8;
		if (this.shooting) flags |= 16;
		if (this.reloading) flags |= 32;
		buffer.putInt(flags);
		buffer.putFloat(this.mouseLocation.x());
		buffer.putFloat(this.mouseLocation.y());
		return buffer.array();
	}

	public static PlayerControlState fromBytes(byte[] bytes) {
		var s = new PlayerControlState();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		int flags = buffer.getInt();
		s.movingLeft = (flags & 1) > 0;
		s.movingRight = (flags & 2) > 0;
		s.movingForward = (flags & 4) > 0;
		s.movingBackward = (flags & 8) > 0;
		s.shooting = (flags & 16) > 0;
		s.reloading = (flags & 32) > 0;
		s.mouseLocation = new Vec2(buffer.getFloat(), buffer.getFloat());
		return s;
	}
}
