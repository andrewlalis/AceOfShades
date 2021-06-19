package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.model.tools.Gun;

import java.util.Objects;

public class Player extends PhysicsObject {
	public static final double MOVEMENT_SPEED = 10; // Movement speed, in m/s
	public static final double RADIUS = 0.5; // Collision radius, in meters.

	private final int id;
	private final String name;
	private Team team;
	private PlayerControlState state;
	private Gun gun;

	private transient long lastShot;
	private transient long reloadingStartedAt;
	private boolean reloading;

	public Player(int id, String name, Team team) {
		this.id = id;
		this.name = name;
		this.team = team;
		this.state = new PlayerControlState();
		this.state.setPlayerId(this.id);
		this.gun = Gun.m1Garand();
		this.useWeapon();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setState(PlayerControlState state) {
		this.state = state;
	}

	public PlayerControlState getState() {
		return state;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Gun getGun() {
		return gun;
	}

	public void setGun(Gun gun) {
		this.gun = gun;
	}

	public long getLastShot() {
		return lastShot;
	}

	public boolean canUseWeapon() {
		return this.state.isShooting() &&
			!this.state.isReloading() &&
			!this.reloading &&
			this.gun.getCurrentClipBulletCount() > 0 &&
			this.lastShot + this.gun.getShotCooldownTime() * 1000 < System.currentTimeMillis();
	}

	public void useWeapon() {
		this.lastShot = System.currentTimeMillis();
		this.gun.decrementBulletCount();
	}

	public void startReloading() {
		this.reloading = true;
		this.reloadingStartedAt = System.currentTimeMillis();
	}

	public void finishReloading() {
		this.gun.reload();
		this.reloading = false;
	}

	public boolean isReloadingComplete() {
		long msSinceStart = System.currentTimeMillis() - this.reloadingStartedAt;
		if (msSinceStart > this.gun.getReloadTime() * 1000) {
			return true;
		}
		return false;
	}

	public boolean isReloading() {
		return reloading;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Player player = (Player) o;
		return getId() == player.getId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
