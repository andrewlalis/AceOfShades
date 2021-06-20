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
			.add(player.getOrientation().mul(1.5))
			.add(player.getOrientation().perp().mul(Player.RADIUS))
		);
		this.setOrientation(player.getOrientation());

		Random r = ThreadLocalRandom.current();
		Vec2 perturbation = new Vec2((r.nextDouble() - 0.5) * 2, (r.nextDouble() - 0.5) * 2).mul(player.getGun().getAccuracy());
		this.setVelocity(this.getOrientation().add(perturbation).mul(player.getGun().getBulletSpeed()));
		this.gun = player.getGun();
	}

	public int getPlayerId() {
		return playerId;
	}

	public Gun getGun() {
		return gun;
	}
}
