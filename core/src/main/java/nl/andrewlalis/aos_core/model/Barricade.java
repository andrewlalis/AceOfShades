package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;

import java.io.Serializable;

public class Barricade implements Serializable {
	/**
	 * The top-left position of this barricade, measured as the distance in
	 * meters from the top-left corner of the map.
	 */
	private Vec2 position;

	/**
	 * The size of the barricade, in meters.
	 */
	private Vec2 size;

	public Barricade(Vec2 position, Vec2 size) {
		this.position = position;
		this.size = size;
	}

	public Barricade(double x, double y, double w, double h) {
		this(new Vec2(x, y), new Vec2(w, h));
	}

	public Vec2 getPosition() {
		return position;
	}

	public Vec2 getSize() {
		return size;
	}
}
