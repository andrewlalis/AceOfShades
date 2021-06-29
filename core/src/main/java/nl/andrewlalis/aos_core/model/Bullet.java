package nl.andrewlalis.aos_core.model;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.tools.Gun;

public class Bullet extends PhysicsObject {
	private final int playerId;
	private final Player player;
	private final Gun gun;

	public Bullet(Player player) {
		this.playerId = player.getId();
		this.player = player;
		this.setPosition(player.getPosition()
			.add(player.getOrientation().mul(1.5f))
			.add(player.getOrientation().perp().mul(Player.RADIUS))
		);
		this.setOrientation(player.getOrientation());
		float accuracy = player.getGun().getType().getAccuracy();
		if (player.isSneaking()) {
			accuracy *= Player.SNEAK_ACCURACY_MODIFIER;
		} else if (player.isSprinting()) {
			accuracy *= Player.SPRINT_ACCURACY_MODIFIER;
		}
		Vec2 perturbation = Vec2.random(-1, 1).mul(accuracy);
		Vec2 localVelocity = this.getOrientation().add(perturbation).mul(player.getGun().getType().getBulletSpeed());
		this.setVelocity(player.getVelocity().add(localVelocity));
		this.gun = player.getGun();
	}

	public Bullet(Vec2 position, Vec2 velocity) {
		super(position, new Vec2(0, -1), velocity);
		this.playerId = -1;
		this.player = null;
		this.gun = null;
	}

	public int getPlayerId() {
		return playerId;
	}

	public Player getPlayer() {
		return player;
	}

	public Gun getGun() {
		return gun;
	}
}
