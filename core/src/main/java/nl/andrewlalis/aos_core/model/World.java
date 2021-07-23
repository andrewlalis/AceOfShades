package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.tools.GunType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The main game world, consisting of all players and other objects in the game.
 */
public class World implements Serializable {
	private final Vec2 size;

	private final Map<Byte, Team> teams;
	private final Map<String, GunType> gunTypes;
	private final Map<Integer, Player> players;
	private final List<Bullet> bullets;
	private final List<Barricade> barricades;

	public World(Vec2 size) {
		this.size = size;
		this.teams = new ConcurrentHashMap<>();
		this.gunTypes = new ConcurrentHashMap<>();
		this.players = new ConcurrentHashMap<>();
		this.bullets = new CopyOnWriteArrayList<>();
		this.barricades = new ArrayList<>();
	}

	public Vec2 getSize() {
		return size;
	}

	public Map<Byte, Team> getTeams() {
		return teams;
	}

	public Map<String, GunType> getGunTypes() {
		return gunTypes;
	}

	public GunType getGunTypeById(byte id) {
		for (var t : this.gunTypes.values()) {
			if (t.id() == id) return t;
		}
		return null;
	}

	public Map<Integer, Player> getPlayers() {
		return this.players;
	}

	public List<Bullet> getBullets() {
		return bullets;
	}

	public List<Barricade> getBarricades() {
		return barricades;
	}
}
