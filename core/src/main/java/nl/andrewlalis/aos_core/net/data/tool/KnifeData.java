package nl.andrewlalis.aos_core.net.data.tool;

import nl.andrewlalis.aos_core.model.World;
import nl.andrewlalis.aos_core.model.tools.Knife;
import nl.andrewlalis.aos_core.model.tools.Tool;

import java.nio.ByteBuffer;

public class KnifeData extends ToolData {
	public KnifeData() {
		super((byte) 2);
	}

	public KnifeData(Knife knife) {
		super((byte) 2);
	}

	@Override
	public int getByteSize() {
		return 1;
	}

	@Override
	protected void putData(ByteBuffer buffer) {
		buffer.put((byte) 0);
	}

	@Override
	protected void getData(ByteBuffer buffer) {
		buffer.get();
	}

	@Override
	public Tool toTool(World world) {
		return new Knife();
	}
}
