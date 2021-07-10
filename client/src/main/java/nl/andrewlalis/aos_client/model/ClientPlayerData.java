package nl.andrewlalis.aos_client.model;

import nl.andrewlalis.aos_core.net.data.tool.ToolData;

import java.util.List;

/**
 * Information about the client's player, which contains more detailed data than
 * what is provided for any random player in the world.
 */
public class ClientPlayerData extends PlayerData {
	private float health;
	private List<ToolData> tools;
	private int selectedToolIndex;

	public float getHealth() {
		return health;
	}

	public List<ToolData> getTools() {
		return tools;
	}

	public int getSelectedToolIndex() {
		return selectedToolIndex;
	}
}
