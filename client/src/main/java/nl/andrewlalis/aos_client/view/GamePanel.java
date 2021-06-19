package nl.andrewlalis.aos_client.view;

import nl.andrewlalis.aos_client.Client;
import nl.andrewlalis.aos_core.model.*;
import nl.andrewlalis.aos_core.net.chat.ChatMessage;
import nl.andrewlalis.aos_core.net.chat.PlayerChatMessage;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class GamePanel extends JPanel {
	private final Client client;

	private final double[] scales = {1.0, 2.5, 5.0, 10.0,  15.0, 20.0, 25.0, 30.0, 35.0};
	private int scaleIndex = 3;

	public GamePanel(Client client) {
		this.client = client;
	}

	public void incrementScale() {
		if (scaleIndex < scales.length - 1) {
			scaleIndex++;
		}
	}

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
		if (world != null) drawWorld(g2, world);
		drawChat(g2, world);
	}

	private void drawWorld(Graphics2D g2, World world) {
		Player myPlayer = world.getPlayers().get(this.client.getPlayerId());
		if (myPlayer == null) return;
		double scale = this.scales[this.scaleIndex];
		AffineTransform pre = g2.getTransform();
		g2.setTransform(this.getWorldTransform(myPlayer, scale));
		g2.setStroke(new BasicStroke((float) (1 / scale)));

		this.drawField(g2, world);
		this.drawPlayers(g2, world);
		this.drawBullets(g2, world);
		g2.setTransform(pre);
	}

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

		for (Team t : world.getTeams()) {
			g2.setColor(t.getColor());
			Ellipse2D.Double spawnCircle = new Ellipse2D.Double(
				t.getSpawnPoint().x() - Player.RADIUS,
				t.getSpawnPoint().y() - Player.RADIUS,
				Player.RADIUS * 2,
				Player.RADIUS * 2
			);
			g2.draw(spawnCircle);
		}
	}

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

			g2.setColor(Color.GRAY);
			Rectangle2D.Double gun = new Rectangle2D.Double(
				0,
				0.5,
				2,
				0.25
			);
			g2.fill(gun);

			g2.setTransform(pre);
		}
	}

	private void drawBullets(Graphics2D g2, World world) {
		g2.setColor(Color.YELLOW);
		double bulletSize = 0.5;
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

	private void drawChat(Graphics2D g2, World world) {
		int height = g2.getFontMetrics().getHeight();
		int y = height;
		for (ChatMessage message : this.client.getLatestChatMessages()) {
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
			} else if (message instanceof PlayerChatMessage pcm) {
				String author = Integer.toString(pcm.getPlayerId());
				if (world != null) {
					Player p = world.getPlayers().get(pcm.getPlayerId());
					if (p != null) author = p.getName();
				}
				text = author + ": " + text;
			}
			g2.setColor(color);
			g2.drawString(text, 5, y);
			y += height;
		}

		if (this.client.isChatting()) {
			g2.setColor(Color.WHITE);
			g2.drawString("> " + this.client.getCurrentChatBuffer(), 5, height * 11);
		}
	}
}
