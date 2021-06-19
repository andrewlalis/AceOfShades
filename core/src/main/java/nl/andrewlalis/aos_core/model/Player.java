package nl.andrewlalis.aos_core.model;

import java.util.Objects;

public class Player extends PhysicsObject {
	public static final double SHOT_COOLDOWN = 0.1; // Time between shots, in seconds.
	public static final double MOVEMENT_SPEED = 10; // Movement speed, in m/s
	public static final double RADIUS = 0.5; // Collision radius, in meters.

	private final int id;
	private final String name;
	private Team team;
	private PlayerControlState state;

	private transient long lastShot;

	public Player(int id, String name, Team team) {
		this.id = id;
		this.name = name;
		this.team = team;
		this.state = new PlayerControlState();
		this.state.setPlayerId(this.id);
		this.updateLastShot();
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

	public long getLastShot() {
		return lastShot;
	}

	public void updateLastShot() {
		this.lastShot = System.currentTimeMillis();
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
