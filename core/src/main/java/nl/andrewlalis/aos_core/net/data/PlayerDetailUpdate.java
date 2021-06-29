package nl.andrewlalis.aos_core.net.data;

import nl.andrewlalis.aos_core.model.Player;

import java.nio.ByteBuffer;

public class PlayerDetailUpdate {
	public static final int BYTES = Float.BYTES + 1 + 5 * Integer.BYTES;

	private final float health;
	private final boolean reloading;

	private final int gunMaxClipCount;
	private final int gunClipSize;
	private final int gunBulletsPerRound;
	private final int gunCurrentClipBulletCount;
	private final int gunClipCount;

	public PlayerDetailUpdate(Player player) {
		this.health = player.getHealth();
		this.reloading = player.isReloading();

		this.gunMaxClipCount = player.getGun().getType().getMaxClipCount();
		this.gunClipSize = player.getGun().getType().getClipSize();
		this.gunBulletsPerRound = player.getGun().getType().getBulletsPerRound();
		this.gunCurrentClipBulletCount = player.getGun().getCurrentClipBulletCount();
		this.gunClipCount = player.getGun().getClipCount();
	}

	private PlayerDetailUpdate(float health, boolean reloading, int gunMaxClipCount, int gunClipSize, int gunBulletsPerRound, int gunCurrentClipBulletCount, int gunClipCount) {
		this.health = health;
		this.reloading = reloading;
		this.gunMaxClipCount = gunMaxClipCount;
		this.gunClipSize = gunClipSize;
		this.gunBulletsPerRound = gunBulletsPerRound;
		this.gunCurrentClipBulletCount = gunCurrentClipBulletCount;
		this.gunClipCount = gunClipCount;
	}

	public float getHealth() {
		return health;
	}

	public boolean isReloading() {
		return reloading;
	}

	public int getGunMaxClipCount() {
		return gunMaxClipCount;
	}

	public int getGunClipSize() {
		return gunClipSize;
	}

	public int getGunBulletsPerRound() {
		return gunBulletsPerRound;
	}

	public int getGunCurrentClipBulletCount() {
		return gunCurrentClipBulletCount;
	}

	public int getGunClipCount() {
		return gunClipCount;
	}

	@Override
	public String toString() {
		return "PlayerDetailUpdate{" +
			"health=" + health +
			", reloading=" + reloading +
			", gunMaxClipCount=" + gunMaxClipCount +
			", gunClipSize=" + gunClipSize +
			", gunBulletsPerRound=" + gunBulletsPerRound +
			", gunCurrentClipBulletCount=" + gunCurrentClipBulletCount +
			", gunClipCount=" + gunClipCount +
			'}';
	}

	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(BYTES);
		buffer.putFloat(health);
		buffer.put((byte) (this.reloading ? 1 : 0));
		buffer.putInt(this.gunMaxClipCount);
		buffer.putInt(this.gunClipSize);
		buffer.putInt(this.gunBulletsPerRound);
		buffer.putInt(this.gunCurrentClipBulletCount);
		buffer.putInt(this.gunClipCount);
		return buffer.array();
	}

	public static PlayerDetailUpdate fromBytes(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return new PlayerDetailUpdate(
			buffer.getFloat(),
			buffer.get() == 1,
			buffer.getInt(),
			buffer.getInt(),
			buffer.getInt(),
			buffer.getInt(),
			buffer.getInt()
		);
	}
}
