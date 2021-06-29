package nl.andrewlalis.aos_core.model.tools;

import java.io.Serializable;

public class Gun implements Serializable {
	GunType type;

	/**
	 * Number of bullets left in the current clip.
	 */
	private int currentClipBulletCount;
	/**
	 * Number of clips remaining.
	 */
	private int clipCount;

	public Gun(GunType type, int currentClipBulletCount, int clipCount) {
		this.type = type;
		this.currentClipBulletCount = currentClipBulletCount;
		this.clipCount = clipCount;
	}

	public Gun(GunType type) {
		this(type, 0, type.getMaxClipCount());
	}

	public GunType getType() {
		return type;
	}

	public int getCurrentClipBulletCount() {
		return currentClipBulletCount;
	}

	public int getClipCount() {
		return clipCount;
	}

	public void refillClips() {
		this.clipCount = this.type.getMaxClipCount();
	}

	public void decrementBulletCount() {
		this.currentClipBulletCount = Math.max(this.currentClipBulletCount - 1, 0);
	}

	public void emptyCurrentClip() {
		this.currentClipBulletCount = 0;
	}

	public boolean canReload() {
		return this.clipCount > 0;
	}

	public void reload() {
		if (this.clipCount > 0) {
			this.clipCount--;
			this.currentClipBulletCount = this.type.getClipSize();
		}
	}
}
