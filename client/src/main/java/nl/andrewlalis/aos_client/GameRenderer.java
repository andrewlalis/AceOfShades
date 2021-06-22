package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_client.view.GamePanel;
import nl.andrewlalis.aos_core.model.Bullet;
import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.World;

/**
 * This thread is responsible for updating the client's display periodically,
 * and performing 'dumb' updates of the model in the interim period between
 * updates from the server, by continuing objects' velocities.
 */
public class GameRenderer extends Thread {
	public static final double FPS = 120.0;
	public static final double MS_PER_FRAME = 1000.0 / FPS;

	private volatile boolean running = true;

	private final Client client;
	private final GamePanel gamePanel;

	public GameRenderer(Client client, GamePanel gamePanel) {
		this.client = client;
		this.gamePanel = gamePanel;
	}

	public void shutdown() {
		this.running = false;
	}

	@Override
	public void run() {
		long lastFrame = System.currentTimeMillis();
		while (this.running) {
			long now = System.currentTimeMillis();
			long msSinceLastFrame = now - lastFrame;
			if (msSinceLastFrame >= MS_PER_FRAME) {
				float elapsedSeconds = msSinceLastFrame / 1000.0f;
				this.gamePanel.repaint();
				this.updateWorld(elapsedSeconds);
				lastFrame = now;
				msSinceLastFrame = 0;
			}
			long msUntilNextFrame = (long) (MS_PER_FRAME - msSinceLastFrame);
			if (msUntilNextFrame > 0) {
				try {
					Thread.sleep(msUntilNextFrame);
				} catch (InterruptedException e) {
					System.err.println("Interrupted while waiting for next frame: " + e.getMessage());
				}
			}
		}
	}

	private void updateWorld(float t) {
		World world = this.client.getWorld();
		for (Player p : world.getPlayers().values()) {
			p.setPosition(p.getPosition().add(p.getVelocity().mul(t)));
		}
		for (Bullet b : world.getBullets()) {
			b.setPosition(b.getPosition().add(b.getVelocity().mul(t)));
		}
	}
}
