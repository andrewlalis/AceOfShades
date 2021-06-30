package nl.andrewlalis.aos_server.settings;

import java.util.List;

public class ServerSettings {
	private int port;
	private int maxPlayers;
	private float ticksPerSecond;

	private RegistrySettings registrySettings;
	private PlayerSettings playerSettings;
	private TeamSettings teamSettings;
	private List<GunSettings> gunSettings;

	public int getPort() {
		return port;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public float getTicksPerSecond() {
		return ticksPerSecond;
	}

	public RegistrySettings getRegistrySettings() {
		return registrySettings;
	}

	public PlayerSettings getPlayerSettings() {
		return playerSettings;
	}

	public TeamSettings getTeamSettings() {
		return teamSettings;
	}

	public List<GunSettings> getGunSettings() {
		return gunSettings;
	}
}
