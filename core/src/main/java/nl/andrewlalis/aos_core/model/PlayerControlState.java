package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * This object represents the player's controls at a given point in time. This
 * object is sent by the client to the server each time the client's inputs
 * change somehow.
 */
public class PlayerControlState implements Serializable {
	boolean movingLeft;
	boolean movingRight;
	boolean movingForward;
	boolean movingBackward;
	boolean sprinting;
	boolean sneaking;

	boolean usingTool;
	boolean reloading;

	boolean selectingPreviousTool;
	boolean selectingNextTool;

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

	public boolean isSprinting() {
		return sprinting;
	}

	public void setSprinting(boolean sprinting) {
		this.sprinting = sprinting;
	}

	public boolean isSneaking() {
		return sneaking;
	}

	public void setSneaking(boolean sneaking) {
		this.sneaking = sneaking;
	}

	public boolean isUsingTool() {
		return usingTool;
	}

	public void setUsingTool(boolean usingTool) {
		this.usingTool = usingTool;
	}

	public boolean isReloading() {
		return reloading;
	}

	public void setReloading(boolean reloading) {
		this.reloading = reloading;
	}

	public boolean isSelectingPreviousTool() {
		return selectingPreviousTool;
	}

	public void setSelectingPreviousTool(boolean selectingPreviousTool) {
		this.selectingPreviousTool = selectingPreviousTool;
	}

	public boolean isSelectingNextTool() {
		return selectingNextTool;
	}

	public void setSelectingNextTool(boolean selectingNextTool) {
		this.selectingNextTool = selectingNextTool;
	}

	public Vec2 getMouseLocation() {
		return mouseLocation;
	}

	public void setMouseLocation(Vec2 mouseLocation) {
		this.mouseLocation = mouseLocation;
	}

	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + 2 * Float.BYTES);
		int flags = 0;
		if (this.movingLeft) flags |= 1;
		if (this.movingRight) flags |= 2;
		if (this.movingForward) flags |= 4;
		if (this.movingBackward) flags |= 8;
		if (this.usingTool) flags |= 16;
		if (this.reloading) flags |= 32;
		if (this.sprinting) flags |= 64;
		if (this.sneaking) flags |= 128;
		if (this.selectingPreviousTool) flags |= 256;
		if (this.selectingNextTool) flags |= 512;
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
		s.usingTool = (flags & 16) > 0;
		s.reloading = (flags & 32) > 0;
		s.sprinting = (flags & 64) > 0;
		s.sneaking = (flags & 128) > 0;
		s.selectingPreviousTool = (flags & 256) > 0;
		s.selectingNextTool = (flags & 512) > 0;
		s.mouseLocation = new Vec2(buffer.getFloat(), buffer.getFloat());
		return s;
	}
}
