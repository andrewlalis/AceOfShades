package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Bullet extends PhysicsObject {
	private final int playerId;

	public Bullet(Player player) {
		super(
			player.getPosition().add(player.getOrientation().mul(1.5)),
			player.getOrientation(),
			null
		);
		this.playerId = player.getId();
		Random r = ThreadLocalRandom.current();
		Vec2 perturbation = new Vec2((r.nextDouble() - 0.5) * 2, (r.nextDouble() - 0.5) * 2).mul(player.getGun().getAccuracy());
		this.setVelocity(player.getOrientation().add(perturbation).mul(player.getGun().getBulletSpeed()));
	}

	public int getPlayerId() {
		return playerId;
	}
}
