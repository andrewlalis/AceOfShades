package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.*;
import nl.andrewlalis.aos_core.model.tools.GunType;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;
import nl.andrewlalis.aos_core.net.data.Sound;
import nl.andrewlalis.aos_core.net.data.SoundType;
import nl.andrewlalis.aos_core.net.data.WorldUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WorldUpdater extends Thread {
	public static final double TARGET_TPS = 120.0;
	public static final double MS_PER_TICK = 1000.0 / TARGET_TPS;

	private final Server server;
	private final World world;

	private final WorldUpdate worldUpdate;

	private volatile boolean running = true;

	public WorldUpdater(Server server, World world) {
		this.server = server;
		this.world = world;
		this.worldUpdate = new WorldUpdate();
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
				float elapsedSeconds = msSinceLastTick / 1000.0f;
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

	private void tick(float t) {
		this.worldUpdate.clear();
		this.updateBullets(t);
		this.updatePlayers(t);
		this.server.sendWorldUpdate(this.worldUpdate);
	}

	private void updatePlayers(float t) {
		for (Player p : this.world.getPlayers().values()) {
			this.updatePlayerMovement(p, t);
			this.updatePlayerShooting(p);
			this.worldUpdate.addPlayer(p);
		}
	}

	private void updatePlayerMovement(Player p, float t) {
		if (p.getState().getMouseLocation() != null && p.getState().getMouseLocation().mag() > 0) {
			Vec2 newOrientation = p.getState().getMouseLocation().unit();
			if (p.getTeam() != null) {
				double theta = p.getTeam().getOrientation().rotate(Math.PI / 2).angle();
				newOrientation = newOrientation.rotate(-theta);
			}
			p.setOrientation(newOrientation);
		}
		float vx = 0;
		float vy = 0;
		if (p.getState().isMovingForward()) vy += Player.MOVEMENT_SPEED;
		if (p.getState().isMovingBackward()) vy -= Player.MOVEMENT_SPEED;
		if (p.getState().isMovingLeft()) vx -= Player.MOVEMENT_SPEED;
		if (p.getState().isMovingRight()) vx += Player.MOVEMENT_SPEED;
		Vec2 forwardVector = new Vec2(0, -1);
		if (p.getTeam() != null) {
			forwardVector = p.getTeam().getOrientation();
		}
		Vec2 leftVector = forwardVector.perp();
		Vec2 newPos = p.getPosition().add(forwardVector.mul(vy * t)).add(leftVector.mul(vx * t));
		float nx = newPos.x();
		float ny = newPos.y();

		for (Barricade b : world.getBarricades()) {
			// TODO: Improve barricade collision smoothness.
			float x1 = b.getPosition().x();
			float x2 = x1 + b.getSize().x();
			float y1 = b.getPosition().y();
			float y2 = y1 + b.getSize().y();
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

		if (p.canResupply()) {
			p.resupply();
		}
	}

	private void updatePlayerShooting(Player p) {
		if (p.canUseWeapon()) {
			for (int i = 0; i < p.getGun().getBulletsPerRound(); i++) {
				Bullet b = new Bullet(p);
				this.world.getBullets().add(b);
				this.worldUpdate.addBullet(b);
			}
			SoundType soundType = SoundType.SHOT_SMG;
			if (p.getGun().getType() == GunType.RIFLE) {
				soundType = SoundType.SHOT_RIFLE;
			} else if (p.getGun().getType() == GunType.SHOTGUN) {
				soundType = SoundType.SHOT_SHOTGUN;
			}
			this.worldUpdate.addSound(new Sound(p.getPosition(), 1.0f, soundType));
			p.useWeapon();
		}
		if (p.getState().isReloading() && !p.isReloading() && p.getGun().canReload()) {
			p.startReloading();
		}
		if (p.isReloading() && p.isReloadingComplete()) {
			p.finishReloading();
			this.worldUpdate.addSound(new Sound(p.getPosition(), 1.0f, SoundType.RELOAD));
		}
	}

	private void updateBullets(float t) {
		List<Bullet> bulletsToRemove = new ArrayList<>();
		for (Bullet b : this.world.getBullets()) {
			Vec2 oldPos = b.getPosition();
			b.setPosition(b.getPosition().add(b.getVelocity().mul(t)));
			Vec2 pos = b.getPosition();
			if (pos.x() < 0 || pos.y() < 0 || pos.x() > this.world.getSize().x() || pos.y() > this.world.getSize().y()) {
				bulletsToRemove.add(b);
				continue;
			}
			boolean removed = false;
			for (Barricade bar : this.world.getBarricades()) {
				if (
					pos.x() > bar.getPosition().x() && pos.x() < bar.getPosition().x() + bar.getSize().x() &&
					pos.y() > bar.getPosition().y() && pos.y() < bar.getPosition().y() + bar.getSize().y()
				) {
					int code = ThreadLocalRandom.current().nextInt(SoundType.BULLET_IMPACT_1.getCode(), SoundType.BULLET_IMPACT_5.getCode() + 1);
					this.worldUpdate.addSound(new Sound(b.getPosition(), 1.0f, SoundType.get((byte) code)));
					bulletsToRemove.add(b);
					removed = true;
					break;
				}
			}
			if (removed) continue;

			float x1 = oldPos.x();
			float x2 = b.getPosition().x();
			float y1 = oldPos.y();
			float y2 = b.getPosition().y();
			float lineDist = oldPos.dist(b.getPosition());
			for (Player p : this.world.getPlayers().values()) {
				float n = ((p.getPosition().x() - x1) * (x2 - x1) + (p.getPosition().y() - y1) * (y2 - y1)) / lineDist;
				n = Math.max(Math.min(n, 1), 0);
				double dist = p.getPosition().dist(new Vec2(x1 + n * (x2 - x1), y1 + n * (y2 - y1)));
				if (dist < Player.RADIUS && (p.getTeam() == null || p.getTeam().getSpawnPoint().dist(p.getPosition()) > Team.SPAWN_RADIUS)) {

					// Player was shot!
					float damage = (float) (((Player.RADIUS - dist) / Player.RADIUS) * b.getGun().getBaseDamage());
					p.takeDamage(damage);
					if (p.getHealth() == 0.0f) {
						Player shooter = this.world.getPlayers().get(b.getPlayerId());
						this.server.broadcastMessage(new SystemChatMessage(SystemChatMessage.Level.SEVERE, p.getName() + " was shot by " + shooter.getName() + "."));
						this.worldUpdate.addSound(new Sound(p.getPosition(), 1.0f, SoundType.DEATH));
						if (shooter.getTeam() != null) {
							shooter.getTeam().incrementScore();
						}
						p.respawn();
					}
				}
			}
			this.worldUpdate.addBullet(b);
		}
		this.world.getBullets().removeAll(bulletsToRemove);
	}
}
