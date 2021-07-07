package nl.andrewlalis.aos_server.settings;

public class GunSettings {
	private String name;
	private String category;
	private String color;
	private int maxClipCount;
	private int clipSize;
	private int bulletsPerRound;
	private float accuracy;
	private float shotCooldownTime;
	private float reloadTime;
	private float bulletSpeed;
	private float baseDamage;
	private float recoil;

	public String getName() {
		return name;
	}

	public String getCategory() {
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

	public float getRecoil() {
		return recoil;
	}
}
