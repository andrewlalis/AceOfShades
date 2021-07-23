package nl.andrewlalis.aos_core.model.tools;

public class Knife implements Tool {
	/**
	 * @return The name of the tool, as it should be shown to the players.
	 */
	@Override
	public String getName() {
		return "Knife";
	}

	/**
	 * Uses the tool.
	 */
	@Override
	public void use() {

	}

	/**
	 * Resupplies the tool, when a player is resupplied at their team's area.
	 */
	@Override
	public void resupply() {

	}

	/**
	 * Resets the tool to its preferred initial state. This is useful for things
	 * like respawning.
	 */
	@Override
	public void reset() {

	}

	/**
	 * @return True if the player may use the tool to perform an action, or
	 * false if it's not possible to do so.
	 */
	@Override
	public boolean isUsable() {
		return true;
	}
}
