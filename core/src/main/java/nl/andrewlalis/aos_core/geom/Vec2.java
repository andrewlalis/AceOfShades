package nl.andrewlalis.aos_core.geom;

import java.io.Serializable;

public record Vec2(double x, double y) implements Serializable {

	public double mag() {
		return Math.sqrt(x * x + y * y);
	}

	public Vec2 add(Vec2 other) {
		return new Vec2(this.x + other.x, this.y + other.y);
	}

	public Vec2 sub(Vec2 other) {
		return new Vec2(this.x - other.x, this.y - other.y);
	}

	public Vec2 mul(double factor) {
		return new Vec2(this.x * factor, this.y * factor);
	}

	public Vec2 unit() {
		double mag = this.mag();
		return new Vec2(this.x / mag, this.y / mag);
	}

	public double dot(Vec2 other) {
		return this.x * other.x + this.y * other.y;
	}

	public Vec2 perp() {
		return new Vec2(-this.y, this.x);
	}

	public Vec2 perp2() {
		return new Vec2(this.y, -this.x);
	}

	public double dist(Vec2 other) {
		return other.sub(this).mag();
	}

	public Vec2 rotate(double theta) {
		return new Vec2(
			this.x * Math.cos(theta) - this.y * Math.sin(theta),
			this.x * Math.sin(theta) + this.y * Math.cos(theta)
		);
	}

	public double angle() {
		return Math.atan2(this.y, this.x);
	}

	@Override
	public String toString() {
		return "[ " + x + ", " + y + " ]";
	}
}
