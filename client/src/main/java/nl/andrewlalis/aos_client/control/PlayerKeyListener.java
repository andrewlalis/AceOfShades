package nl.andrewlalis.aos_client.control;

import nl.andrewlalis.aos_client.Client;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PlayerKeyListener extends KeyAdapter {
	private final Client client;

	public PlayerKeyListener(Client client) {
		this.client = client;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (!this.client.isChatting() && (e.getKeyChar() == 't' || e.getKeyChar() == '/')) {
			this.client.setChatting(true);
			if (e.getKeyChar() == '/') this.client.appendToChat('/');
		} else if (this.client.isChatting()) {
			char c = e.getKeyChar();
			if (c >= ' ' && c <= '~') {
				this.client.appendToChat(c);
			} else if (e.getKeyChar() == 8) {
				this.client.backspaceChat();
			} else if (e.getKeyChar() == 10) {
				this.client.sendChat();
			} else if (e.getKeyChar() == 27) {
				this.client.setChatting(false);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (client.isChatting()) return;
		var state = client.getPlayerState();
		if (e.getKeyCode() == KeyEvent.VK_W) {
			state.setMovingForward(true);
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			state.setMovingBackward(true);
		} else if (e.getKeyCode() == KeyEvent.VK_A) {
			state.setMovingLeft(true);
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			state.setMovingRight(true);
		}
		this.client.sendPlayerState();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (client.isChatting()) return;
		var state = client.getPlayerState();
		if (e.getKeyCode() == KeyEvent.VK_W) {
			state.setMovingForward(false);
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			state.setMovingBackward(false);
		} else if (e.getKeyCode() == KeyEvent.VK_A) {
			state.setMovingLeft(false);
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			state.setMovingRight(false);
		}
		this.client.sendPlayerState();
	}
}
