package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.Barricade;
import nl.andrewlalis.aos_core.model.Bullet;
import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.World;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WorldUpdater extends Thread {
	public static final double TARGET_TPS = 120.0;
	public static final double MS_PER_TICK = 1000.0 / TARGET_TPS;

	private final Server server;
	private final World world;
	private volatile boolean running = true;

	public WorldUpdater(Server server, World world) {
		this.server = server;
		this.world = world;
	}

	public void shutdown() {
		this.running = false;
	}

	@Override
	public void run() {
		long lastTick = System.currentTimeMillis();
		while (this.running) {
			long now = System.currentTimeMillis();
			long msSinceLastTick = now - lastTick;
			if (msSinceLastTick >= MS_PER_TICK) {
				double elapsedSeconds = msSinceLastTick / 1000.0;
				this.tick(elapsedSeconds);
				lastTick = now;
			}
			long msUntilNextTick = (long) (MS_PER_TICK - msSinceLastTick);
			if (msUntilNextTick > 0) {
				try {
					Thread.sleep(msUntilNextTick);
				} catch (InterruptedException e) {
					System.err.println("Interrupted while sleeping until next tick: " + e.getMessage());
				}
			}
		}
	}

	private void tick(double t) {
		world.getSoundsToPlay().clear();
		this.updateBullets(t);
		this.updatePlayers(t);
		this.server.sendWorldToClients();
	}

	private void updatePlayers(double t) {
		for (Player p : this.world.getPlayers().values()) {
			this.updatePlayerMovement(p, t);
			this.updatePlayerShooting(p);
		}
	}

	private void updatePlayerMovement(Player p, double t) {
		if (p.getState().getMouseLocation() != null && p.getState().getMouseLocation().mag() > 0) {
			Vec2 newOrientation = p.getState().getMouseLocation().unit();
			if (p.getTeam() != null) {
				double theta = p.getTeam().getOrientation().rotate(Math.PI / 2).angle();
				newOrientation = newOrientation.rotate(-theta);
			}
			p.setOrientation(newOrientation);
		}
		double vx = 0;
		double vy = 0;
		if (p.getState().isMovingForward()) vy += Player.MOVEMENT_SPEED;
		if (p.getState().isMovingBackward()) vy -= Player.MOVEMENT_SPEED;
		if (p.getState().isMovingLeft()) vx -= Player.MOVEMENT_SPEED;
		if (p.getState().isMovingRight()) vx += Player.MOVEMENT_SPEED;
		Vec2 forwardVector = p.getOrientation().mul(vy);
		Vec2 leftVector = p.getOrientation().perp().mul(vx);
		Vec2 newPos = p.getPosition().add(forwardVector.mul(t)).add(leftVector.mul(t));
		double nx = newPos.x();
		double ny = newPos.y();

		for (Barricade b : world.getBarricades()) {
			// TODO: Improve barricade collision smoothness.
			double x1 = b.getPosition().x();
			double x2 = x1 + b.getSize().x();
			double y1 = b.getPosition().y();
			double y2 = y1 + b.getSize().y();
			if (nx + Player.RADIUS > x1 && nx - Player.RADIUS < x2 && ny + Player.RADIUS > y1 && ny - Player.RADIUS < y2) {
				double distanceLeft = Math.abs(nx - x1);
				double distanceRight = Math.abs(nx - x2);
				double distanceTop = Math.abs(ny - y1);
				double distanceBottom = Math.abs(ny - y2);
				if (distanceLeft < Player.RADIUS) {
					nx = x1 - Player.RADIUS;
				} else if (distanceRight < Player.RADIUS) {
					nx = x2 + Player.RADIUS;
				} else if (distanceTop < Player.RADIUS) {
					ny = y1 - Player.RADIUS;
				} else if (distanceBottom < Player.RADIUS) {
					ny = y2 + Player.RADIUS;
				}
			}
		}

		if (nx - Player.RADIUS < 0) nx = Player.RADIUS;
		if (nx + Player.RADIUS > this.world.getSize().x()) nx = this.world.getSize().x() - Player.RADIUS;
		if (ny - Player.RADIUS < 0) ny = Player.RADIUS;
		if (ny + Player.RADIUS > this.world.getSize().y()) ny = this.world.getSize().y() - Player.RADIUS;
		p.setPosition(new Vec2(nx, ny));
	}

	private void updatePlayerShooting(Player p) {
		if (p.canUseWeapon()) {
			this.world.getBullets().add(new Bullet(p));
			this.world.getSoundsToPlay().add("ak47shot1.wav");
			p.useWeapon();
		}
		if (p.getState().isReloading() && !p.isReloading() && p.getGun().canReload()) {
			p.startReloading();
		}
		if (p.isReloading() && p.isReloadingComplete()) {
			p.finishReloading();
		}
	}

	private void updateBullets(double t) {
		List<Bullet> bulletsToRemove = new ArrayList<>();
		for (Bullet b : this.world.getBullets()) {
			Vec2 oldPos = b.getPosition();
			b.setPosition(b.getPosition().add(b.getVelocity().mul(t)));
			Vec2 pos = b.getPosition();
			if (pos.x() < 0 || pos.y() < 0 || pos.x() > this.world.getSize().x() || pos.y() > this.world.getSize().y()) {
				bulletsToRemove.add(b);
			}
			for (Barricade bar : this.world.getBarricades()) {
				if (
					pos.x() > bar.getPosition().x() && pos.x() < bar.getPosition().x() + bar.getSize().x() &&
					pos.y() > bar.getPosition().y() && pos.y() < bar.getPosition().y() + bar.getSize().y()
				) {
					int n = ThreadLocalRandom.current().nextInt(1, 6);
					this.world.getSoundsToPlay().add("bullet_impact_" + n + ".wav");
					bulletsToRemove.add(b);
					break;
				}
			}

			double x1 = oldPos.x();
			double x2 = b.getPosition().x();
			double y1 = oldPos.y();
			double y2 = b.getPosition().y();
			double lineDist = oldPos.dist(b.getPosition());
			for (Player p : this.world.getPlayers().values()) {
				double n = ((p.getPosition().x() - x1) * (x2 - x1) + (p.getPosition().y() - y1) * (y2 - y1)) / lineDist;
				n = Math.max(Math.min(n, 1), 0);
				double dist = p.getPosition().dist(new Vec2(x1 + n * (x2 - x1), y1 + n * (y2 - y1)));
				if (dist < Player.RADIUS) {
					Player killer = this.world.getPlayers().get(b.getPlayerId());
					this.server.broadcastMessage(new SystemChatMessage(SystemChatMessage.Level.SEVERE, p.getName() + " was shot by " + killer.getName() + "."));
					world.getSoundsToPlay().add("death.wav");
					if (p.getTeam() != null) {
						p.setPosition(p.getTeam().getSpawnPoint());
					}
				}
			}
		}
		this.world.getBullets().removeAll(bulletsToRemove);
	}
}
