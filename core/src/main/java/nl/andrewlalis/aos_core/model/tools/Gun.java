package nl.andrewlalis.aos_core.model.tools;

/**
 * A type of tool that, when equipped, allows the player to shoot bullets.
 */
public class Gun implements Tool {
	GunType type;

	/**
	 * Number of bullets left in the current clip.
	 */
	private int currentClipBulletCount;
	/**
	 * Number of clips remaining.
	 */
	private int clipCount;

	private transient long lastShot;
	private transient long reloadingStartedAt;
	private boolean reloading;

	public Gun(GunType type, int currentClipBulletCount, int clipCount) {
		this.type = type;
		this.currentClipBulletCount = currentClipBulletCount;
		this.clipCount = clipCount;

		this.lastShot = System.currentTimeMillis();
	}

	public Gun(GunType type) {
		this(type, 0, type.maxClipCount());
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

	public void decrementBulletCount() {
		this.currentClipBulletCount = Math.max(this.currentClipBulletCount - 1, 0);
	}

	public void emptyCurrentClip() {
		this.currentClipBulletCount = 0;
	}

	public boolean canReload() {
		return this.clipCount > 0;
	}

	public boolean isReloading() {
		return reloading;
	}

	public void startReloading() {
		this.reloading = true;
		this.reloadingStartedAt = System.currentTimeMillis();
	}

	public boolean isReloadingComplete() {
		return this.reloading && (System.currentTimeMillis() - this.reloadingStartedAt) > this.type.reloadTime() * 1000;
	}

	public void reload() {
		if (this.clipCount > 0) {
			this.clipCount--;
			this.currentClipBulletCount = this.type.clipSize();
		}
		this.reloading = false;
	}

	@Override
	public void use() {
		this.lastShot = System.currentTimeMillis();
		this.currentClipBulletCount--;
	}

	@Override
	public void resupply() {
		this.clipCount = this.type.maxClipCount();
	}

	@Override
	public boolean isUsable() {
		return !this.reloading &&
				this.currentClipBulletCount > 0 &&
				this.lastShot + (this.type.shotCooldownTime() * 1000) < System.currentTimeMillis();
	}
}
