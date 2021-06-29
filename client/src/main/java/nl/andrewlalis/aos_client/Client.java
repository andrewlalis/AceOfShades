package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_client.view.GameFrame;
import nl.andrewlalis.aos_client.view.GamePanel;
import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.World;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.net.data.DataTypes;
import nl.andrewlalis.aos_core.net.data.PlayerDetailUpdate;
import nl.andrewlalis.aos_core.net.data.WorldUpdate;

import java.io.IOException;

/**
 * The main class for the client, which connects to a server to join and play.
 */
public class Client {
	private final MessageTransceiver messageTransceiver;

	private World world;
	private Player myPlayer;

	private final GameRenderer renderer;
	private final SoundManager soundManager;
	private final ChatManager chatManager;

	private final GameFrame frame;

	public Client(String serverHost, int serverPort, String username) throws IOException {
		this.soundManager = new SoundManager();
		this.chatManager = new ChatManager(this.soundManager);
		this.messageTransceiver = new MessageTransceiver(this, serverHost, serverPort, username);
		this.messageTransceiver.start();
		this.chatManager.bindTransceiver(this.messageTransceiver);

		while (this.myPlayer == null || this.world == null) {
			try {
				System.out.println("Waiting for server response and player registration...");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Player and world data initialized.");
		GamePanel gamePanel = new GamePanel(this);
		this.renderer = new GameRenderer(this, gamePanel);
		this.frame = new GameFrame("Ace of Shades - " + serverHost + ":" + serverPort, this, gamePanel);
		this.frame.setVisible(true);
		this.renderer.start();
	}

	public ChatManager getChatManager() {
		return chatManager;
	}

	public World getWorld() {
		return world;
	}

	public void updateWorld(WorldUpdate update) {
		if (this.world == null) return;
		this.world.getBullets().clear();
		for (var u : update.getBulletUpdates()) {
			this.world.getBullets().add(u.toBullet());
		}
		for (var p : update.getPlayerUpdates()) {
			Player player = this.world.getPlayers().get(p.getId());
			if (player != null) {
				player.setPosition(p.getPosition());
				player.setOrientation(p.getOrientation());
				player.setVelocity(p.getVelocity());
				player.setGun(new Gun(this.world.getGunTypes().get(p.getGunTypeName())));
			}
		}
		this.soundManager.play(update.getSoundsToPlay());
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public void setPlayer(Player player) {
		this.myPlayer = player;
	}

	public Player getPlayer() {
		return myPlayer;
	}

	public void updatePlayer(PlayerDetailUpdate update) {
		if (this.myPlayer == null) return;
		this.myPlayer.setHealth(update.getHealth());
		this.myPlayer.setReloading(update.isReloading());
		this.myPlayer.setGun(new Gun(this.myPlayer.getGun().getType(), update.getGunCurrentClipBulletCount(), update.getGunClipCount()));
	}

	public void sendPlayerState() {
		try {
			this.messageTransceiver.sendData(DataTypes.PLAYER_CONTROL_STATE, myPlayer.getId(), myPlayer.getState().toBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		this.chatManager.unbindTransceiver();
		System.out.println("Chat manager shutdown.");
		this.messageTransceiver.shutdown();
		System.out.println("Message transceiver shutdown.");
		this.renderer.shutdown();
		System.out.println("Renderer shutdown.");
		this.soundManager.close();
		System.out.println("Sound manager closed.");
		this.frame.dispose();
	}
}
