package nl.andrewlalis.aos_client.control;

import nl.andrewlalis.aos_client.net.ChatManager;
import nl.andrewlalis.aos_client.Client;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PlayerKeyListener extends KeyAdapter {
	private final Client client;
	private final ChatManager chatManager;

	public PlayerKeyListener(Client client) {
		this.client = client;
		this.chatManager = client.getChatManager();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (!this.chatManager.isChatting()) {
			if ((e.getKeyChar() == 't' || e.getKeyChar() == '/')) {
				this.chatManager.setChatting(true);
				var s = this.client.getPlayer().getState();
				s.setMovingForward(false);
				s.setMovingBackward(false);
				s.setMovingLeft(false);
				s.setMovingRight(false);
				s.setReloading(false);
				s.setSneaking(false);
				s.setSprinting(false);
				this.client.sendPlayerState();
				if (e.getKeyChar() == '/') this.chatManager.appendToChat('/');
			}
		} else if (this.chatManager.isChatting()) {
			char c = e.getKeyChar();
			if (c >= ' ' && c <= '~') {
				this.chatManager.appendToChat(c);
			} else if (e.getKeyChar() == 8) {
				this.chatManager.backspaceChat();
			} else if (e.getKeyChar() == 10) {
				this.chatManager.sendChat();
			} else if (e.getKeyChar() == 27) {
				this.chatManager.setChatting(false);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (this.chatManager.isChatting()) return;
		var state = client.getPlayer().getState();
		if (e.getKeyCode() == KeyEvent.VK_W) {
			state.setMovingForward(true);
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			state.setMovingBackward(true);
		} else if (e.getKeyCode() == KeyEvent.VK_A) {
			state.setMovingLeft(true);
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			state.setMovingRight(true);
		} else if (e.getKeyCode() == KeyEvent.VK_R) {
			state.setReloading(true);
		} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			state.setSprinting(true);
		} else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			state.setSneaking(true);
		}
		this.client.sendPlayerState();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (this.chatManager.isChatting()) return;
		var state = client.getPlayer().getState();
		if (e.getKeyCode() == KeyEvent.VK_W) {
			state.setMovingForward(false);
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			state.setMovingBackward(false);
		} else if (e.getKeyCode() == KeyEvent.VK_A) {
			state.setMovingLeft(false);
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			state.setMovingRight(false);
		} else if (e.getKeyCode() == KeyEvent.VK_R) {
			state.setReloading(false);
		} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			state.setSprinting(false);
		} else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			state.setSneaking(false);
		}
		this.client.sendPlayerState();
	}
}
