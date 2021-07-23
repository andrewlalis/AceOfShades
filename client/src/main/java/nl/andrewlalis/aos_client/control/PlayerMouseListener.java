package nl.andrewlalis.aos_client.control;

import nl.andrewlalis.aos_client.Client;
import nl.andrewlalis.aos_client.view.GamePanel;
import nl.andrewlalis.aos_core.geom.Vec2;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Listens for changes to the player's mouse state. This handles the following
 * possible control events:
 * <ul>
 *     <li>Pressing the mouse to use the player's weapon.</li>
 *     <li>Scrolling the mouse wheel to zoom in or out.</li>
 *     <li>Moving the mouse to change the player's orientation.</li>
 * </ul>
 */
public class PlayerMouseListener extends MouseInputAdapter {
	private static final float MOUSE_UPDATES_PER_SECOND = 60.0f;
	private static final long MS_PER_MOUSE_UPDATE = (long) (1000.0f / MOUSE_UPDATES_PER_SECOND);

	private final Client client;
	private final GamePanel gamePanel;

	private long lastMouseMove = 0L;

	public PlayerMouseListener(Client client, GamePanel gamePanel) {
		this.client = client;
		this.gamePanel = gamePanel;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			client.getPlayer().getState().setUsingTool(true);
			client.sendControlState();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			client.getPlayer().getState().setUsingTool(false);
			client.sendControlState();
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
		Vec2 c = new Vec2(this.gamePanel.getWidth() / 2.0f, this.gamePanel.getHeight() / 2.0f);
		Vec2 centeredMouseLocation = new Vec2(e.getX(), e.getY()).sub(c);
		client.getPlayer().getState().setMouseLocation(centeredMouseLocation);
		long now = System.currentTimeMillis();
		if (now - this.lastMouseMove > MS_PER_MOUSE_UPDATE) {
			client.sendControlState();
			this.lastMouseMove = now;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Vec2 c = new Vec2(this.gamePanel.getWidth() / 2.0f, this.gamePanel.getHeight() / 2.0f);
		Vec2 centeredMouseLocation = new Vec2(e.getX(), e.getY()).sub(c);
		client.getPlayer().getState().setMouseLocation(centeredMouseLocation);
		client.getPlayer().getState().setUsingTool(true);
		long now = System.currentTimeMillis();
		if (now - this.lastMouseMove > MS_PER_MOUSE_UPDATE) {
			client.sendControlState();
			this.lastMouseMove = now;
		}
	}
}
