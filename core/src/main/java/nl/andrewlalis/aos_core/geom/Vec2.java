package nl.andrewlalis.aos_core.geom;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public record Vec2(float x, float y) implements Serializable {
	public static final Vec2 ZERO = new Vec2(0, 0);
	public static final Vec2 UP = new Vec2(0, -1);
	public static final Vec2 DOWN = new Vec2(0, 1);
	public static final Vec2 RIGHT = new Vec2(1, 0);
	public static final Vec2 LEFT = new Vec2(-1, 0);


	public float mag() {
		return (float) Math.sqrt(x * x + y * y);
	}

	public Vec2 add(Vec2 other) {
		return new Vec2(this.x + other.x, this.y + other.y);
	}

	public Vec2 sub(Vec2 other) {
		return new Vec2(this.x - other.x, this.y - other.y);
	}

	public Vec2 mul(float factor) {
		return new Vec2(this.x * factor, this.y * factor);
	}

	public Vec2 unit() {
		float mag = this.mag();
		return new Vec2(this.x / mag, this.y / mag);
	}

	public float dot(Vec2 other) {
		return this.x * other.x + this.y * other.y;
	}

	public Vec2 perp() {
		return new Vec2(-this.y, this.x);
	}

	public Vec2 perp2() {
		return new Vec2(this.y, -this.x);
	}

	public float dist(Vec2 other) {
		return other.sub(this).mag();
	}

	public Vec2 rotate(double theta) {
		return new Vec2(
			(float) (this.x * Math.cos(theta) - this.y * Math.sin(theta)),
			(float) (this.x * Math.sin(theta) + this.y * Math.cos(theta))
		);
	}

	public double angle() {
		return Math.atan2(this.y, this.x);
	}

	@Override
	public String toString() {
		return "[ " + x + ", " + y + " ]";
	}

	public static Vec2 random(float min, float max) {
		Random r = ThreadLocalRandom.current();
		float x = r.nextFloat() * (max - min) + min;
		float y = r.nextFloat() * (max - min) + min;
		return new Vec2(x, y);
	}

	public static Vec2 read(DataInputStream in) throws IOException {
		return new Vec2(in.readFloat(), in.readFloat());
	}
}
