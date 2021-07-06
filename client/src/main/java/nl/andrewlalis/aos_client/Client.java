package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_client.net.ChatManager;
import nl.andrewlalis.aos_client.net.MessageTransceiver;
import nl.andrewlalis.aos_client.sound.SoundManager;
import nl.andrewlalis.aos_client.view.GameFrame;
import nl.andrewlalis.aos_client.view.GamePanel;
import nl.andrewlalis.aos_client.view.GameRenderer;
import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.Team;
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

	/**
	 * Initializes and starts the client, connecting immediately to a server
	 * according to the given host, port, and username.
	 * @param serverHost The server's host name or ip to connect to.
	 * @param serverPort The server's port to connect to.
	 * @param username The player's username to use when connecting.
	 * @throws IOException If the connection could not be initialized.
	 */
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

	/**
	 * Updates the client's version of the world according to an update packet
	 * that was received from the server.
	 * @param update The update packet from the server.
	 */
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
		for (var t : update.getTeamUpdates()) {
			Team team = this.world.getTeams().get(t.getId());
			if (team != null) {
				team.setScore(t.getScore());
			}
		}
		this.soundManager.play(update.getSoundsToPlay(), myPlayer);
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

	/**
	 * Updates the client's own player data according to an update from the
	 * server.
	 * @param update The updated player information from the server.
	 */
	public void updatePlayer(PlayerDetailUpdate update) {
		if (this.myPlayer == null) return;
		this.myPlayer.setHealth(update.getHealth());
		this.myPlayer.setReloading(update.isReloading());
		this.myPlayer.setGun(new Gun(this.myPlayer.getGun().getType(), update.getGunCurrentClipBulletCount(), update.getGunClipCount()));
	}

	/**
	 * Sends a player control state message to the server, which indicates that
	 * the player's controls have been updated, due to a key or mouse event.
	 */
	public void sendPlayerState() {
		try {
			this.messageTransceiver.sendData(DataTypes.PLAYER_CONTROL_STATE, myPlayer.getId(), myPlayer.getState().toBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shuts down the client.
	 */
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
