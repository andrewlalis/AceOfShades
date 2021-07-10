package nl.andrewlalis.aos_core.model.tools;

/**
 * The grenade tool, when equipped, allows the player to throw grenades into the
 * world, if there are some grenades available.
 */
public class Grenade implements Tool {
	private final int maxGrenades = 3;

	private int grenades;

	public Grenade(int grenades) {
		this.grenades = grenades;
	}

	public Grenade() {
		this.grenades = maxGrenades;
	}

	public int getGrenadesRemaining() {
		return grenades;
	}

	public int getMaxGrenades() {
		return maxGrenades;
	}

	@Override
	public void use() {
		this.grenades--;
	}

	@Override
	public void resupply() {
		this.grenades = this.maxGrenades;
	}

	@Override
	public void reset() {
		this.resupply();
	}

	@Override
	public boolean isUsable() {
		return this.grenades > 0;
	}
}
