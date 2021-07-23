package nl.andrewlalis.aos_core.net.data.tool;

import nl.andrewlalis.aos_core.model.World;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.model.tools.Tool;

import java.nio.ByteBuffer;

public class GunData extends ToolData {
	private byte typeId;
	private boolean reloading;
	private int clipCount;
	private int currentClipBulletCount;
	private int bulletsPerRound;
	private int clipSize;
	private int maxClipCount;

	public GunData() {
		super((byte) 0);
	}

	public GunData(Gun gun) {
		this();
		this.typeId = gun.getType().id();
		this.reloading = gun.isReloading();
		this.clipCount = gun.getClipCount();
		this.currentClipBulletCount = gun.getCurrentClipBulletCount();
		this.bulletsPerRound = gun.getType().bulletsPerRound();
		this.clipSize = gun.getType().clipSize();
		this.maxClipCount = gun.getType().maxClipCount();
	}

	public byte getTypeId() {
		return typeId;
	}

	public boolean isReloading() {
		return reloading;
	}

	public int getClipCount() {
		return clipCount;
	}

	public int getCurrentClipBulletCount() {
		return currentClipBulletCount;
	}

	public int getBulletsPerRound() {
		return bulletsPerRound;
	}

	public int getClipSize() {
		return clipSize;
	}

	public int getMaxClipCount() {
		return maxClipCount;
	}

	@Override
	public int getByteSize() {
		return 2 * Byte.BYTES + 5 * Integer.BYTES;
	}

	@Override
	protected void putData(ByteBuffer buffer) {
		buffer.put(this.getTypeId());
		buffer.put((byte) (this.reloading ? 1 : 0));
		buffer.putInt(this.clipCount);
		buffer.putInt(this.currentClipBulletCount);
		buffer.putInt(this.bulletsPerRound);
		buffer.putInt(this.clipSize);
		buffer.putInt(this.maxClipCount);
	}

	@Override
	protected void getData(ByteBuffer buffer) {
		this.typeId = buffer.get();
		this.reloading = buffer.get() == 1;
		this.clipCount = buffer.getInt();
		this.currentClipBulletCount = buffer.getInt();
		this.bulletsPerRound = buffer.getInt();
		this.clipSize = buffer.getInt();
		this.maxClipCount = buffer.getInt();
	}

	@Override
	public Tool toTool(World world) {
		return new Gun(world.getGunTypeById(this.getTypeId()), this.currentClipBulletCount, this.clipCount, this.reloading);
	}
}
