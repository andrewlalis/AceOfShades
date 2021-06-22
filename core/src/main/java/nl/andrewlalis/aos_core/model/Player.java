package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.tools.Gun;

import java.util.Objects;

public class Player extends PhysicsObject implements Comparable<Player> {
	public static final float MOVEMENT_SPEED = 10; // Movement speed, in m/s
	public static final float RADIUS = 0.5f; // Collision radius, in meters.
	public static final float RESUPPLY_COOLDOWN = 30; // Seconds between allowing resupply.
	public static final float MAX_HEALTH = 100.0f;

	private final int id;
	private final String name;
	private Team team;
	private PlayerControlState state;
	private Gun gun;
	private float health;

	private transient long lastShot;
	private transient long reloadingStartedAt;
	private boolean reloading;
	private transient long lastResupply;

	public Player(int id, String name, Team team) {
		this.id = id;
		this.name = name;
		this.team = team;
		this.state = new PlayerControlState();
		this.gun = Gun.ak47();
		this.health = MAX_HEALTH;
		this.useWeapon();
		this.lastShot = System.currentTimeMillis();
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

	public void setHealth(float health) {
		this.health = health;
	}

	public void setReloading(boolean reloading) {
		this.reloading = reloading;
	}

	public boolean canUseWeapon() {
		return this.state.isShooting() &&
			!this.state.isReloading() &&
			!this.reloading &&
			this.gun.getCurrentClipBulletCount() > 0 &&
			this.lastShot + ((long) (this.gun.getShotCooldownTime() * 1000)) < System.currentTimeMillis() &&
			(this.getTeam() == null || this.getTeam().getSpawnPoint().dist(this.getPosition()) > Team.SPAWN_RADIUS);
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

	public boolean canResupply() {
		return this.team != null &&
			this.team.getSupplyPoint().dist(this.getPosition()) < Team.SUPPLY_POINT_RADIUS &&
			System.currentTimeMillis() - this.lastResupply > RESUPPLY_COOLDOWN * 1000;
	}

	public void resupply() {
		this.lastResupply = System.currentTimeMillis();
		this.gun.refillClips();
		this.health = MAX_HEALTH;
	}

	public float getHealth() {
		return health;
	}

	public void takeDamage(float damage) {
		this.health = Math.max(this.health - damage, 0.0f);
	}

	public void respawn() {
		this.resupply();
		this.gun.emptyCurrentClip();
		if (this.team != null) {
			this.setPosition(this.team.getSpawnPoint().add(Vec2.random(-Team.SPAWN_RADIUS / 2, Team.SPAWN_RADIUS / 2)));
		}
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

	@Override
	public int compareTo(Player o) {
		int r = this.name.compareTo(o.getName());
		if (r == 0) return Integer.compare(this.id, o.getId());
		return r;
	}
}
