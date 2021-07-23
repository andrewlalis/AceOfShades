package nl.andrewlalis.aos_core.net.data;

import nl.andrewlalis.aos_core.geom.Vec2;
import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.tools.Grenade;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.model.tools.Knife;
import nl.andrewlalis.aos_core.net.data.tool.GrenadeData;
import nl.andrewlalis.aos_core.net.data.tool.GunData;
import nl.andrewlalis.aos_core.net.data.tool.KnifeData;
import nl.andrewlalis.aos_core.net.data.tool.ToolData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The data that's sent to all clients about a player, and contains only the
 * information needed to render the player on the screen.
 */
public class PlayerUpdate {
	private final int id;
	private final Vec2 position;
	private final Vec2 orientation;
	private final Vec2 velocity;
	private final ToolData selectedTool;

	public PlayerUpdate(Player player) {
		this.id = player.getId();
		this.position = player.getPosition();
		this.orientation = player.getOrientation();
		this.velocity = player.getVelocity();
		if (player.getSelectedTool() instanceof Knife knife) {
			this.selectedTool = new KnifeData(knife);
		} else if (player.getSelectedTool() instanceof Gun gun) {
			this.selectedTool = new GunData(gun);
		} else if (player.getSelectedTool() instanceof Grenade grenade) {
			this.selectedTool = new GrenadeData(grenade);
		} else {
			throw new IllegalArgumentException("Invalid selected tool.");
		}
	}

	public PlayerUpdate(int id, Vec2 position, Vec2 orientation, Vec2 velocity, ToolData selectedTool) {
		this.id = id;
		this.position = position;
		this.orientation = orientation;
		this.velocity = velocity;
		this.selectedTool = selectedTool;
	}

	public int getId() {
		return id;
	}

	public Vec2 getPosition() {
		return position;
	}

	public Vec2 getOrientation() {
		return orientation;
	}

	public Vec2 getVelocity() {
		return velocity;
	}

	public ToolData getSelectedTool() {
		return selectedTool;
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeInt(this.id);
		out.writeFloat(this.position.x());
		out.writeFloat(this.position.y());
		out.writeFloat(this.orientation.x());
		out.writeFloat(this.orientation.y());
		out.writeFloat(this.velocity.x());
		out.writeFloat(this.velocity.y());
		out.writeInt(1 + this.selectedTool.getByteSize());
		ByteBuffer buffer = ByteBuffer.allocate(1 + this.selectedTool.getByteSize());
		this.selectedTool.write(buffer);
		out.write(buffer.array());
	}

	public static PlayerUpdate read(DataInputStream in) throws IOException {
		int id = in.readInt();
		Vec2 position = Vec2.read(in);
		Vec2 orientation = Vec2.read(in);
		Vec2 velocity = Vec2.read(in);
		int toolByteSize = in.readInt();
		ByteBuffer buffer = ByteBuffer.wrap(in.readNBytes(toolByteSize));
		ToolData tool = ToolData.read(buffer);
		return new PlayerUpdate(id, position, orientation, velocity, tool);
	}
}
