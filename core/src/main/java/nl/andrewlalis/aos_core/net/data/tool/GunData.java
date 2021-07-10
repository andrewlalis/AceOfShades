package nl.andrewlalis.aos_core.net.data.tool;

import nl.andrewlalis.aos_core.model.tools.Gun;

import java.nio.ByteBuffer;

public class GunData extends ToolData {
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
		this.reloading = gun.isReloading();
		this.clipCount = gun.getClipCount();
		this.currentClipBulletCount = gun.getCurrentClipBulletCount();
		this.bulletsPerRound = gun.getType().bulletsPerRound();
		this.clipSize = gun.getType().clipSize();
		this.maxClipCount = gun.getType().maxClipCount();
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
		return 1 + 5 * Integer.BYTES;
	}

	@Override
	protected void putData(ByteBuffer buffer) {
		buffer.put((byte) (this.reloading ? 1 : 0));
		buffer.putInt(this.clipCount);
		buffer.putInt(this.currentClipBulletCount);
		buffer.putInt(this.bulletsPerRound);
		buffer.putInt(this.clipSize);
		buffer.putInt(this.maxClipCount);
	}

	@Override
	protected void getData(ByteBuffer buffer) {
		this.reloading = buffer.get() == 1;
		this.clipCount = buffer.getInt();
		this.currentClipBulletCount = buffer.getInt();
		this.bulletsPerRound = buffer.getInt();
		this.clipSize = buffer.getInt();
		this.maxClipCount = buffer.getInt();
	}
}
