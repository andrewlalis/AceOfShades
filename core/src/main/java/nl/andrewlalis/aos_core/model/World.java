package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main game world, consisting of all players and other objects in the game.
 */
public class World implements Serializable {
	private final Vec2 size;

	private final List<Team> teams;
	private final Map<Integer, Player> players;
	private final List<Bullet> bullets;
	private final List<Barricade> barricades;

	private final List<String> soundsToPlay;

	public World(Vec2 size) {
		this.size = size;
		this.teams = new ArrayList<>();
		this.players = new HashMap<>();
		this.bullets = new ArrayList<>();
		this.barricades = new ArrayList<>();
		this.soundsToPlay = new ArrayList<>();
	}

	public Vec2 getSize() {
		return size;
	}

	public List<Team> getTeams() {
		return teams;
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

	public List<String> getSoundsToPlay() {
		return soundsToPlay;
	}
}
