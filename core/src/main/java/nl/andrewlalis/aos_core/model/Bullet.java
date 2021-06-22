package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.tools.Gun;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Bullet extends PhysicsObject {
	private final int playerId;
	private final Gun gun;

	public Bullet(Player player) {
		this.playerId = player.getId();
		this.setPosition(player.getPosition()
			.add(player.getOrientation().mul(1.5f))
			.add(player.getOrientation().perp().mul(Player.RADIUS))
		);
		this.setOrientation(player.getOrientation());
		Vec2 perturbation = Vec2.random(-1, 1).mul(player.getGun().getAccuracy());
		this.setVelocity(this.getOrientation().add(perturbation).mul(player.getGun().getBulletSpeed()));
		this.gun = player.getGun();
	}

	public Bullet(Vec2 position, Vec2 velocity) {
		super(position, new Vec2(0, -1), velocity);
		this.playerId = -1;
		this.gun = null;
	}

	public int getPlayerId() {
		return playerId;
	}

	public Gun getGun() {
		return gun;
	}
}
