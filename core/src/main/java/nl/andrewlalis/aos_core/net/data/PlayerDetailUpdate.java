package nl.andrewlalis.aos_core.net.data;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.tools.Grenade;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.model.tools.Tool;
import nl.andrewlalis.aos_core.net.data.tool.GrenadeData;
import nl.andrewlalis.aos_core.net.data.tool.GunData;
import nl.andrewlalis.aos_core.net.data.tool.ToolData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PlayerDetailUpdate {
	private final float health;

	private List<ToolData> tools;
	private int selectedToolIndex;

	public PlayerDetailUpdate(Player player) {
		this.health = player.getHealth();
		this.tools = new ArrayList<>(player.getTools().size());
		for (int i = 0; i < player.getTools().size(); i++) {
			var t = player.getTools().get(i);
			if (t instanceof Gun g) {
				this.tools.add(new GunData(g));
			} else if (t instanceof Grenade g) {
				this.tools.add(new GrenadeData(g));
			}
			if (t.equals(player.getSelectedTool())) {
				selectedToolIndex = i;
			}
		}
	}

	private PlayerDetailUpdate(float health, List<ToolData> tools, int selectedToolIndex) {
		this.health = health;
	}

	public float getHealth() {
		return health;
	}

	public List<ToolData> getTools() {
		return tools;
	}

	public byte[] toBytes() {
		int size = Float.BYTES + 2 * Integer.BYTES;
		for (var td : this.tools) {
			size += td.getByteSize();
		}
		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.putFloat(this.health);
		buffer.putInt(this.selectedToolIndex);
		buffer.putInt(this.tools.size());
		for (var td : this.tools) {
			td.write(buffer);
		}
		return buffer.array();
	}

	public static PlayerDetailUpdate fromBytes(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		float health = buffer.getFloat();
		int selectedToolIndex = buffer.getInt();
		int toolCount = buffer.getInt();
		List<ToolData> tools = new ArrayList<>(toolCount);
		for (int i = 0; i < toolCount; i++) {
			tools.add(ToolData.read(buffer));
		}
		return new PlayerDetailUpdate(health, tools, selectedToolIndex);
	}
}
