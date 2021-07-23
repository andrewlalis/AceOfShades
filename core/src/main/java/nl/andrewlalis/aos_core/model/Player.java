package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.model.tools.Knife;
import nl.andrewlalis.aos_core.model.tools.Tool;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Player extends PhysicsObject implements Comparable<Player> {
	public static final float MOVEMENT_THRESHOLD = 0.001f; // Threshold for stopping movement. Speeds slower than this are reduced to 0.
	public static final float RADIUS = 0.5f; // Collision radius, in meters.
	public static final float TOOL_CHANGE_TIME = 0.5f; // Cooldown when swapping weapons, in seconds.

	private final int id;
	private final String name;
	private Team team;
	private PlayerControlState state;
	private final List<Tool> tools;
	private int selectedToolIndex;
	private float health;

	private transient long lastResupply;
	private transient long lastToolChange;

	public Player(int id, String name, Team team, float maxHealth) {
		this.id = id;
		this.name = name;
		this.team = team;
		this.state = new PlayerControlState();
		this.health = maxHealth;
		this.tools = new CopyOnWriteArrayList<>();
		this.tools.add(new Knife());
		this.selectedToolIndex = 0;
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
		if (this.tools.isEmpty()) return null;
		return this.tools.get(this.selectedToolIndex);
	}

	public boolean canChangeSelectedTool() {
		return System.currentTimeMillis() - this.lastToolChange > TOOL_CHANGE_TIME * 1000;
	}

	public void setSelectedToolIndex(int index) {
		if (index > this.tools.size() - 1) {
			index = 0;
		} else if (index < 0) {
			index = this.tools.size() - 1;
		}
		this.selectedToolIndex = index;
		this.lastToolChange = System.currentTimeMillis();
	}

	public void selectNextTool() {
		this.setSelectedToolIndex(this.selectedToolIndex + 1);
	}

	public void selectPreviousTool() {
		this.setSelectedToolIndex(this.selectedToolIndex - 1);
	}

	public void setHealth(float health) {
		this.health = health;
	}

	public boolean canUseGun() {
		return this.state.isUsingTool() &&
			this.getSelectedTool() instanceof Gun gun && gun.isUsable() &&
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
