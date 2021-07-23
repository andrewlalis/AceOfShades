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
import nl.andrewlalis.aos_core.model.tools.Grenade;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.model.tools.Knife;
import nl.andrewlalis.aos_core.net.data.DataTypes;
import nl.andrewlalis.aos_core.net.data.PlayerDetailUpdate;
import nl.andrewlalis.aos_core.net.data.WorldUpdate;
import nl.andrewlalis.aos_core.net.data.tool.GrenadeData;
import nl.andrewlalis.aos_core.net.data.tool.GunData;
import nl.andrewlalis.aos_core.net.data.tool.KnifeData;

import java.io.IOException;

/**
 * The main class for the client, which connects to a server to join and play.
 */
public class Client {
	private final MessageTransceiver messageTransceiver;

	private World world;
	private Player player;

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

		while (this.player == null || this.world == null) {
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
				if (player.getVelocity().mag() > 0) {
					this.soundManager.playWalking(player, null);
				}
				player.getTools().clear();
				player.getTools().add(p.getSelectedTool().toTool(this.world));
				player.setSelectedToolIndex(0);
			}
		}
		for (var t : update.getTeamUpdates()) {
			Team team = this.world.getTeams().get(t.getId());
			if (team != null) {
				team.setScore(t.getScore());
			}
		}
		this.soundManager.play(update.getSoundsToPlay(), null);
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Updates the client's own player data according to an update from the
	 * server.
	 * @param update The updated player information from the server.
	 */
	public void updatePlayer(PlayerDetailUpdate update) {
		if (this.player != null) {
			this.player.setHealth(update.getHealth());
			this.player.getTools().clear();
			for (var td : update.getTools()) {
				if (td instanceof KnifeData knifeData) {
					this.player.getTools().add(new Knife());
				} else if (td instanceof GunData gunData) {
					this.player.getTools().add(new Gun(
							this.world.getGunTypeById(gunData.getTypeId()),
							gunData.getCurrentClipBulletCount(),
							gunData.getClipCount(),
							gunData.isReloading()
					));
				} else if (td instanceof GrenadeData grenadeData) {
					this.player.getTools().add(new Grenade(grenadeData.getGrenades(), grenadeData.getMaxGrenades()));
				}
			}
			this.player.setSelectedToolIndex(update.getSelectedToolIndex());
		}
	}

	/**
	 * Sends a player control state message to the server, which indicates that
	 * the player's controls have been updated, due to a key or mouse event.
	 */
	public void sendControlState() {
		try {
			this.messageTransceiver.sendData(DataTypes.PLAYER_CONTROL_STATE, this.player.getId(), this.player.getState().toBytes());
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
