package nl.andrewlalis.aos_client.control;

import nl.andrewlalis.aos_client.Client;
import nl.andrewlalis.aos_client.view.GamePanel;
import nl.andrewlalis.aos_core.geom.Vec2;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class PlayerMouseListener extends MouseInputAdapter {
	private final Client client;
	private final GamePanel gamePanel;

	public PlayerMouseListener(Client client, GamePanel gamePanel) {
		this.client = client;
		this.gamePanel = gamePanel;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			client.getPlayerState().setShooting(true);
			client.sendPlayerState();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			client.getPlayerState().setShooting(false);
			client.sendPlayerState();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() > 0) {
			this.gamePanel.decrementScale();
		} else if (e.getWheelRotation() < 0) {
			this.gamePanel.incrementScale();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Vec2 c = new Vec2(this.gamePanel.getWidth() / 2.0, this.gamePanel.getHeight() / 2.0);
		Vec2 centeredMouseLocation = new Vec2(e.getX(), e.getY()).sub(c);
		client.getPlayerState().setMouseLocation(centeredMouseLocation);
		client.sendPlayerState();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Vec2 c = new Vec2(this.gamePanel.getWidth() / 2.0, this.gamePanel.getHeight() / 2.0);
		Vec2 centeredMouseLocation = new Vec2(e.getX(), e.getY()).sub(c);
		client.getPlayerState().setMouseLocation(centeredMouseLocation);
		client.getPlayerState().setShooting(true);
		client.sendPlayerState();
	}
}
