package nl.andrewlalis.aos_core.model.tools;

/**
 * The grenade tool, when equipped, allows the player to throw grenades into the
 * world, if there are some grenades available.
 */
public class Grenade implements Tool {
	private final int maxGrenades;

	private int grenades;

	public Grenade(int grenades, int maxGrenades) {
		this.grenades = grenades;
		this.maxGrenades = maxGrenades;
	}

	public Grenade() {
		this(3, 3);
	}

	public int getGrenadesRemaining() {
		return grenades;
	}

	public int getMaxGrenades() {
		return maxGrenades;
	}

	/**
	 * @return The name of the tool, as it should be shown to the players.
	 */
	@Override
	public String getName() {
		return "Grenade";
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
