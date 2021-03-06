package nl.andrewlalis.aos_client.view;

import nl.andrewlalis.aos_client.net.ChatManager;
import nl.andrewlalis.aos_client.Client;
import nl.andrewlalis.aos_core.model.*;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.net.chat.ChatMessage;
import nl.andrewlalis.aos_core.net.chat.ChatType;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * The game panel is the component in which the entire game is rendered. It uses
 * a custom implementation of {@link JPanel#paintComponent(Graphics)} to draw
 * the current state of the game.
 */
public class GamePanel extends JPanel {
	/**
	 * A reference to the client that will be the source of game data to render.
	 */
	private final Client client;

	/**
	 * The list of possible view scale factors.
	 */
	private final double[] scales = {1.0, 2.5, 5.0, 10.0,  15.0, 20.0, 25.0, 30.0, 35.0};

	/**
	 * The view's current scale factor, as an index of the
	 * {@link GamePanel#scales} array.
	 */
	private int scaleIndex = 3;

	public GamePanel(Client client) {
		this.client = client;
	}

	/**
	 * Increments the client's view scaling by one index, if possible.
	 */
	public void incrementScale() {
		if (scaleIndex < scales.length - 1) {
			scaleIndex++;
		}
	}

	/**
	 * Decrements the client's view scaling by one index, if possible.
	 */
	public void decrementScale() {
		if (scaleIndex > 0) {
			scaleIndex--;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

		g2.setColor(Color.BLACK);
		g2.setBackground(Color.BLACK);
		g2.clearRect(0, 0, this.getWidth(), this.getHeight());

		World world = client.getWorld();
		if (world != null) {
			drawWorld(g2, world);
			drawStatus(g2, world);
		}
		drawChat(g2);
	}

	/**
	 * Draws the current game world, including all objects, players, bullets,
	 * and other structures.
	 * @param g2 The graphics context.
	 * @param world The world to render.
	 */
	private void drawWorld(Graphics2D g2, World world) {
		Player myPlayer = client.getPlayer();
		if (myPlayer == null) return;
		double scale = this.scales[this.scaleIndex];
		AffineTransform pre = g2.getTransform();
		g2.setTransform(this.getWorldTransform(myPlayer, scale));
		g2.setStroke(new BasicStroke((float) (1 / scale)));

		this.drawField(g2, world);
		this.drawPlayers(g2, world);
		this.drawBullets(g2, world);
		this.drawMarkers(g2, world, myPlayer);

		g2.setTransform(pre);

		// Put shadow gradient.
		RadialGradientPaint p = new RadialGradientPaint(
			this.getWidth() / 2.0f,
			this.getHeight() / 2.0f,
			(float) (25 * scale),
			new float[]{0.0f, 1.0f},
			new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 255)},
			MultipleGradientPaint.CycleMethod.NO_CYCLE
		);
		g2.setPaint(p);
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());
	}

	/**
	 * Obtains a transformation matrix that can be used when rendering the world
	 * so that everything is scaled according to the player's view scaling, and
	 * their team's orientation, and makes sure that the player is in the center
	 * of the view.
	 * @param player The player to get the transform for.
	 * @param scale The current view scale factor.
	 * @return A transformation matrix that can be applied to all subsequent
	 * rendering operations.
	 */
	private AffineTransform getWorldTransform(Player player, double scale) {
		AffineTransform tx = new AffineTransform();
		tx.scale(scale, scale);
		if (player.getTeam() != null) {
			var dir = player.getTeam().getOrientation().perp();
			tx.rotate(dir.x(), dir.y(), (this.getWidth() / scale / 2), (this.getHeight() / scale / 2));
		}
		double x = -player.getPosition().x() + (this.getWidth() / scale / 2);
		double y = -player.getPosition().y() + (this.getHeight() / scale / 2);
		tx.translate(x, y);
		return tx;
	}

	/**
	 * Draws the world's basic shape and objects. This includes the world's own
	 * area, and any fixed barricades in it, as well as various team structures.
	 * @param g2 The graphics context.
	 * @param world The world to draw.
	 */
	private void drawField(Graphics2D g2, World world) {
		g2.setColor(Color.LIGHT_GRAY);
		g2.fill(new Rectangle2D.Double(0, 0, world.getSize().x(), world.getSize().y()));

		g2.setColor(Color.DARK_GRAY);
		for (Barricade b : world.getBarricades()) {
			Rectangle2D.Double barricadeRect = new Rectangle2D.Double(
				b.getPosition().x(),
				b.getPosition().y(),
				b.getSize().x(),
				b.getSize().y()
			);
			g2.fill(barricadeRect);
		}

		for (Team t : world.getTeams().values()) {
			g2.setColor(t.getColor());
			Ellipse2D.Double spawnCircle = new Ellipse2D.Double(
				t.getSpawnPoint().x() - Team.SPAWN_RADIUS,
				t.getSpawnPoint().y() - Team.SPAWN_RADIUS,
				Team.SPAWN_RADIUS * 2,
				Team.SPAWN_RADIUS * 2
			);
			g2.draw(spawnCircle);
			Rectangle2D.Double supplyMarker = new Rectangle2D.Double(
				t.getSupplyPoint().x() - Team.SUPPLY_POINT_RADIUS,
				t.getSupplyPoint().y() - Team.SUPPLY_POINT_RADIUS,
				Team.SUPPLY_POINT_RADIUS * 2,
				Team.SUPPLY_POINT_RADIUS * 2
			);
			g2.draw(supplyMarker);
		}
	}

	/**
	 * Draws all players in the world. In order to make drawing the various
	 * parts of the player easier, we apply a transformation on top of the world
	 * transform, such that the player is at (0, 0) and facing upwards.
	 * @param g2 The graphics context.
	 * @param world The world to get the list of players from.
	 */
	private void drawPlayers(Graphics2D g2, World world) {
		for (Player p : world.getPlayers().values()) {
			AffineTransform pre = g2.getTransform();
			AffineTransform tx = g2.getTransform();
			tx.translate(p.getPosition().x(), p.getPosition().y());
			tx.rotate(p.getOrientation().x(), p.getOrientation().y());
			g2.setTransform(tx);

			Ellipse2D.Double dot = new Ellipse2D.Double(-Player.RADIUS, -Player.RADIUS, Player.RADIUS * 2, Player.RADIUS * 2);
			Color playerColor = p.getTeam() != null ? p.getTeam().getColor() : Color.BLACK;
			g2.setColor(playerColor);
			g2.fill(dot);
			this.drawGun(g2, p.getGun());
			g2.setTransform(pre);
		}
	}

	/**
	 * Draws a player's gun.
	 * @param g2 The graphics context.
	 * @param gun The gun to draw.
	 */
	private void drawGun(Graphics2D g2, Gun gun) {
		g2.setColor(Color.decode(gun.getType().getColor()));
		Rectangle2D.Double gunBarrel = new Rectangle2D.Double(
			0,
			0.5,
			2,
			0.25
		);
		g2.fill(gunBarrel);
	}

	/**
	 * Draws the list of bullets in the world.
	 * @param g2 The graphics context.
	 * @param world The world to get the list of bullets from.
	 */
	private void drawBullets(Graphics2D g2, World world) {
		g2.setColor(Color.BLACK);
		double bulletSize = 0.25;
		for (Bullet b : world.getBullets()) {
			Ellipse2D.Double bulletShape = new Ellipse2D.Double(
				b.getPosition().x() - bulletSize / 2,
				b.getPosition().y() - bulletSize / 2,
				bulletSize,
				bulletSize
			);
			g2.fill(bulletShape);
		}
	}

	/**
	 * Draws player-oriented markers such as the player's name. These are done
	 * in a separate step from the player rendering, to ensure that names are
	 * not obscured by other objects.
	 * @param g2 The graphics context.
	 * @param world The world.
	 * @param myPlayer The reference to this client's player.
	 */
	private void drawMarkers(Graphics2D g2, World world, Player myPlayer) {
		g2.setColor(Color.WHITE);
		for (Player p : world.getPlayers().values()) {
			if (p.getId() == myPlayer.getId()) continue;
			AffineTransform pre = g2.getTransform();
			AffineTransform tx = g2.getTransform();
			tx.translate(p.getPosition().x(), p.getPosition().y());
			tx.rotate(myPlayer.getTeam().getOrientation().perp().angle());
			tx.scale(0.1, 0.1);
			g2.setTransform(tx);
			g2.drawString(p.getName(), 0, 0);
			g2.setTransform(pre);
		}
	}

	/**
	 * Draws the list of chat messages in the top-left corner of the screen.
	 * @param g2 The graphics context.
	 */
	private void drawChat(Graphics2D g2) {
		int height = g2.getFontMetrics().getHeight();
		int y = height;
		var cm = this.client.getChatManager();
		for (ChatMessage message : cm.getLatestChatMessages()) {
			Color color = Color.WHITE;
			String text = message.getText();
			if (message instanceof SystemChatMessage sysMsg) {
				if (sysMsg.getLevel() == SystemChatMessage.Level.INFO) {
					color = Color.YELLOW;
				} else if (sysMsg.getLevel() == SystemChatMessage.Level.WARNING) {
					color = Color.ORANGE;
				} else if (sysMsg.getLevel() == SystemChatMessage.Level.SEVERE) {
					color = Color.RED;
				}
			} else {
				if (message.getChatType() == ChatType.TEAM_PLAYER_CHAT) {
					color = Color.GREEN;
				} else if (message.getChatType() == ChatType.PRIVATE_PLAYER_CHAT) {
					color = Color.CYAN;
				}
			}
			g2.setColor(color);
			g2.drawString(text, 5, y);
			y += height;
		}

		if (cm.isChatting()) {
			g2.setColor(Color.WHITE);
			g2.drawString("> " + cm.getCurrentChatBuffer(), 5, height * (ChatManager.MAX_CHAT_MESSAGES + 1));
		}
	}

	/**
	 * Draws player status information in the bottom-left corner of the screen.
	 * @param g2 The graphics context.
	 * @param world The world containing the player's data.
	 */
	private void drawStatus(Graphics2D g2, World world) {
		Player myPlayer = this.client.getPlayer();
		if (myPlayer == null) return;

		g2.setColor(Color.WHITE);
		if (myPlayer.isReloading()) {
			g2.drawString("Reloading...", 5, this.getHeight() - 10);
		}
		Gun gun = myPlayer.getGun();
		g2.drawString("Clips: " + gun.getClipCount() + " / " + gun.getType().getMaxClipCount(), 5, this.getHeight() - 20);
		g2.drawString("Bullets: " + gun.getCurrentClipBulletCount() + " / " + gun.getType().getClipSize(), 5, this.getHeight() - 30);
		g2.setColor(Color.GREEN);
		g2.drawString(String.format("Health: %.1f", myPlayer.getHealth()), 5, this.getHeight() - 40);

		int y = this.getHeight() - 60;
		for (Team t : world.getTeams().values()) {
			g2.setColor(t.getColor());
			g2.drawString("Team " + t.getName() + ": " + t.getScore(), 5, y);
			y -= 15;
		}
	}
}
