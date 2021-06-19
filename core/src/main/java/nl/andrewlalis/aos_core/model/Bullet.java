package nl.andrewlalis.aos_core.model;

public class Bullet extends PhysicsObject {
	public static final double SPEED = 100.0; // Meters per second.

	private final int playerId;

	public Bullet(Player player) {
		super(player.getPosition().add(player.getOrientation().mul(1.5)), player.getOrientation(), player.getOrientation().mul(SPEED));
		this.playerId = player.getId();
	}

	public int getPlayerId() {
		return playerId;
	}
}
