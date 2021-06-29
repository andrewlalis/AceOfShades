package nl.andrewlalis.aos_core.model.tools;

import java.io.Serializable;

/**
 * Relatively constant configuration information about a particular type of gun,
 * while not including state data for any single gun.
 */
public class GunType implements Serializable {
	/**
	 * The name of this type of gun. Should be unique among all guns in a world.
	 */
	private final String name;
	/**
	 * The category of gun.
	 */
	private final GunCategory category;
	/**
	 * The color of this type of gun, in hex.
	 */
	private final String color;
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
	 * How accurate shots from this gun are. 0 = never miss, 1 = complete random.
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

	public GunType(String name, GunCategory category, String color, int maxClipCount, int clipSize, int bulletsPerRound, float accuracy, float shotCooldownTime, float reloadTime, float bulletSpeed, float baseDamage) {
		this.name = name;
		this.category = category;
		this.color = color;
		this.maxClipCount = maxClipCount;
		this.clipSize = clipSize;
		this.bulletsPerRound = bulletsPerRound;
		this.accuracy = accuracy;
		this.shotCooldownTime = shotCooldownTime;
		this.reloadTime = reloadTime;
		this.bulletSpeed = bulletSpeed;
		this.baseDamage = baseDamage;
	}

	public String getName() {
		return name;
	}

	public GunCategory getCategory() {
		return category;
	}

	public String getColor() {
		return color;
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
}
