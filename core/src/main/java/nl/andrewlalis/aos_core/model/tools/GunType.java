package nl.andrewlalis.aos_core.model.tools;

/**
 * Information about a particular type of gun.
 */
public record GunType(
		String name,
		GunCategory category,
		String color,
		int maxClipCount,
		int clipSize,
		int bulletsPerRound,
		float inaccuracy,
		float shotCooldownTime,
		float reloadTime,
		float bulletSpeed,
		float baseDamage,
		float recoil
) {}
