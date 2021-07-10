package nl.andrewlalis.aos_core.model.tools;

import java.io.Serializable;

/**
 * Represents some sort of usable tool item that players can equip and use.
 */
public interface Tool extends Serializable {
	/**
	 * Uses the tool.
	 */
	void use();

	/**
	 * Resupplies the tool, when a player is resupplied at their team's area.
	 */
	void resupply();

	/**
	 * Resets the tool to its preferred initial state. This is useful for things
	 * like respawning.
	 */
	void reset();

	/**
	 * @return True if the player may use the tool to perform an action, or
	 * false if it's not possible to do so.
	 */
	boolean isUsable();
}
