package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.model.tools.GunType;
import nl.andrewlalis.aos_core.model.tools.Tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Player extends PhysicsObject implements Comparable<Player> {
	public static final float MOVEMENT_THRESHOLD = 0.001f; // Threshold for stopping movement. Speeds slower than this are reduced to 0.
	public static final float RADIUS = 0.5f; // Collision radius, in meters.

	private final int id;
	private final String name;
	private Team team;
	private PlayerControlState state;
	private List<Tool> tools;
	private Tool selectedTool;
	private float health;

	// Stats
	private transient int killCount;
	private transient int deathCount;
	private transient int shotCount;
	private transient int resupplyCount;
	private transient int killStreak;

	private transient long lastResupply;

	public Player(int id, String name, Team team, GunType gunType, float maxHealth) {
		this.id = id;
		this.name = name;
		this.team = team;
		this.state = new PlayerControlState();
		this.health = maxHealth;
		this.tools = new ArrayList<>();
		var gun = new Gun(gunType);
		this.tools.add(gun);
		this.selectedTool = gun;
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

	public List<Tool> getTools() {
		return this.tools;
	}

	public Tool getSelectedTool() {
		return this.selectedTool;
	}

	public void setHealth(float health) {
		this.health = health;
	}

	public boolean canUseGun() {
		return this.state.isShooting() &&
			this.selectedTool instanceof Gun gun && gun.isUsable() &&
			(this.getTeam() == null || this.getTeam().getSpawnPoint().dist(this.getPosition()) > Team.SPAWN_RADIUS);
	}
	public boolean canResupply(float resupplyCooldown) {
		return this.team != null &&
			this.team.getSupplyPoint().dist(this.getPosition()) < Team.SUPPLY_POINT_RADIUS &&
			System.currentTimeMillis() - this.lastResupply > resupplyCooldown * 1000;
	}

	public void resupply(float maxHealth) {
		for (Tool t : this.tools) {
			t.resupply();
		}
		this.health = maxHealth;
		this.lastResupply = System.currentTimeMillis();
		this.resupplyCount++;
	}

	public float getHealth() {
		return health;
	}

	public void takeDamage(float damage) {
		this.health = Math.max(this.health - damage, 0.0f);
	}

	public void respawn(float maxHealth) {
		this.resupply(maxHealth);
		for (Tool t : this.tools) {
			t.reset();
		}
		if (this.team != null) {
			this.setPosition(this.team.getSpawnPoint().add(Vec2.random(-Team.SPAWN_RADIUS / 2, Team.SPAWN_RADIUS / 2)));
		}
	}

	public boolean isSneaking() {
		return this.state.isSneaking() &&
			!this.state.isSprinting();
	}

	public boolean isSprinting() {
		return this.state.isSprinting() &&
			!this.state.isSneaking();
	}

	public int getKillCount() {
		return killCount;
	}

	public int getDeathCount() {
		return deathCount;
	}

	public int getShotCount() {
		return shotCount;
	}

	public int getResupplyCount() {
		return resupplyCount;
	}

	public int getKillStreak() {
		return killStreak;
	}

	public void incrementDeathCount() {
		this.deathCount++;
		this.killStreak = 0;
	}

	public void incrementKillCount() {
		this.killCount++;
		this.killStreak++;
	}

	public void resetStats() {
		this.killCount = 0;
		this.deathCount = 0;
		this.shotCount = 0;
		this.resupplyCount = 0;
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
