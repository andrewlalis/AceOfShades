package nl.andrewlalis.aos_core.net.data.tool;

import nl.andrewlalis.aos_core.model.tools.Grenade;

import java.nio.ByteBuffer;

public class GrenadeData extends ToolData {
	private int grenades;
	private int maxGrenades;

	public GrenadeData() {
		super((byte) 1);
	}

	public GrenadeData(Grenade grenade) {
		this();
		this.grenades = grenade.getGrenadesRemaining();
		this.maxGrenades = grenade.getMaxGrenades();
	}

	@Override
	public int getByteSize() {
		return 2 * Integer.BYTES;
	}

	@Override
	protected void putData(ByteBuffer buffer) {
		buffer.putInt(this.grenades);
		buffer.putInt(this.maxGrenades);
	}

	@Override
	protected void getData(ByteBuffer buffer) {
		this.grenades = buffer.getInt();
		this.maxGrenades = buffer.getInt();
	}
}
