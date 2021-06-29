package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Team implements Serializable {
	public static final float SPAWN_RADIUS = 3;
	public static final float SUPPLY_POINT_RADIUS = 2;

	private final byte id;
	private final String name;
	private final java.awt.Color color;
	private final Vec2 spawnPoint;
	private final Vec2 supplyPoint;
	private final Vec2 orientation;

	private final List<Player> players;

	private int score;

	public Team(byte id, String name, Color color, Vec2 spawnPoint, Vec2 supplyPoint, Vec2 orientation) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.spawnPoint = spawnPoint;
		this.supplyPoint = supplyPoint;
		this.orientation = orientation;
		this.players = new ArrayList<>();
		this.score = 0;
	}

	public byte getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}

	public Vec2 getSpawnPoint() {
		return spawnPoint;
	}

	public Vec2 getSupplyPoint() {
		return supplyPoint;
	}

	public Vec2 getOrientation() {
		return orientation;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public int getScore() {
		return score;
	}

	public void incrementScore() {
		this.score++;
	}

	public void resetScore() {
		this.score = 0;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Team team = (Team) o;
		return getId() == team.getId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
