package nl.andrewlalis.aos_server.settings;

public class PlayerSettings {
	private float speed;
	private float sprintSpeed;
	private float sneakSpeed;
	private float acceleration;
	private float deceleration;
	private float radius;
	private float resupplyCooldown;
	private float maxHealth;
	private float healthRegenRate;
	private float sneakAccuracyModifier;
	private float sprintAccuracyModifier;
	private String defaultGun;

	public float getSpeed() {
		return speed;
	}

	public float getSprintSpeed() {
		return sprintSpeed;
	}

	public float getSneakSpeed() {
		return sneakSpeed;
	}

	public float getAcceleration() {
		return acceleration;
	}

	public float getDeceleration() {
		return deceleration;
	}

	public float getRadius() {
		return radius;
	}

	public float getResupplyCooldown() {
		return resupplyCooldown;
	}

	public float getMaxHealth() {
		return maxHealth;
	}

	public float getHealthRegenRate() {
		return healthRegenRate;
	}

	public float getSneakAccuracyModifier() {
		return sneakAccuracyModifier;
	}

	public float getSprintAccuracyModifier() {
		return sprintAccuracyModifier;
	}

	public String getDefaultGun() {
		return defaultGun;
	}
}
