package nl.andrewlalis.aos_core.model.tools;

import java.io.Serializable;

public class Gun implements Serializable {

	private final GunType type;

	/**
	 * Maximum number of clips a player can carry when using this gun.
	 */
	private final int maxClipCount;
	/**
	 * Number of bullets in each clip.
	 */
	private final int clipSize;

	/**
	 * Number of bullets that are fired simultaneously per round. Usually only
	 * shotguns fire multiple.
	 */
	private final int bulletsPerRound;

	/**
	 * How accurate shots from this gun are.
	 */
	private final float accuracy;

	/**
	 * How long (in seconds) to wait after each shot, before another is shot.
	 */
	private final float shotCooldownTime;

	/**
	 * How long (in seconds) for reloading a new clip.
	 */
	private final float reloadTime;

	/**
	 * How fast the bullet travels (in m/s).
	 */
	private final float bulletSpeed;

	/**
	 * How much damage the bullet does for a direct hit.
	 */
	private final float baseDamage;

	/**
	 * Number of bullets left in the current clip.
	 */
	private int currentClipBulletCount;
	/**
	 * Number of clips remaining.
	 */
	private int clipCount;

	private Gun(GunType type, int maxClipCount, int clipSize, int bulletsPerRound, float accuracy, float shotCooldownTime, float reloadTime, float bulletSpeed, float baseDamage) {
		this.type = type;
		this.maxClipCount = maxClipCount;
		this.clipSize = clipSize;
		this.bulletsPerRound = bulletsPerRound;
		this.accuracy = accuracy;
		this.shotCooldownTime = shotCooldownTime;
		this.reloadTime = reloadTime;
		this.bulletSpeed = bulletSpeed;
		this.baseDamage = baseDamage;

		this.currentClipBulletCount = 0;
		this.clipCount = maxClipCount;
	}

	public GunType getType() {
		return type;
	}

	public int getMaxClipCount() {
		return maxClipCount;
	}

	public int getClipSize() {
		return clipSize;
	}

	public int getBulletsPerRound() {
		return bulletsPerRound;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public float getShotCooldownTime() {
		return shotCooldownTime;
	}

	public float getReloadTime() {
		return reloadTime;
	}

	public float getBulletSpeed() {
		return bulletSpeed;
	}

	public float getBaseDamage() {
		return baseDamage;
	}

	public int getCurrentClipBulletCount() {
		return currentClipBulletCount;
	}

	public int getClipCount() {
		return clipCount;
	}

	public void refillClips() {
		this.clipCount = this.maxClipCount;
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
			this.currentClipBulletCount = this.clipSize;
		}
	}

	/**
	 * Helper method to obtain a "dummy" gun for client-side rendering.
	 * TODO: Improve cleanliness so this isn't necessary.
	 * @param type The type of gun.
	 * @return The gun.
	 */
	public static Gun forType(GunType type) {
		return new Gun(type, -1, -1, -1, -1, -1, -1, -1, -1);
	}

	public static Gun forType(GunType type, int maxClipCount, int clipSize, int bulletsPerRound, int currentClipBulletCount, int clipCount) {
		Gun g = new Gun(type, maxClipCount, clipSize, bulletsPerRound, -1, -1, -1, -1, -1);
		g.currentClipBulletCount = currentClipBulletCount;
		g.clipCount = clipCount;
		return g;
	}

	public static Gun ak47() {
		return new Gun(GunType.SMG, 4, 30, 1, 0.10f, 0.05f, 1.2f, 90, 40);
	}

	public static Gun m1Garand() {
		return new Gun(GunType.RIFLE, 6, 8, 1, 0.02f, 0.75f, 1.5f, 150, 100);
	}

	public static Gun winchester() {
		return new Gun(GunType.SHOTGUN, 8, 4, 3, 0.15f, 0.5f, 2.0f, 75, 60);
	}
}
