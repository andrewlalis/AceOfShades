package nl.andrewlalis.aos_server.command;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_server.Server;

import java.util.ArrayList;
import java.util.List;

public class KickCommand implements Command {
	private final Server server;

	public KickCommand(Server server) {
		this.server = server;
	}

	@Override
	public void execute(String[] args) {
		if (args.length < 1) {
			System.out.println("Missing player id/name argument.");
			return;
		}
		String query = args[0].trim();
		List<Player> matchingPlayers = new ArrayList<>();
		for (var p : this.server.getWorld().getPlayers().values()) {
			if (Integer.toString(p.getId()).equals(query) || p.getName().equals(query)) {
				matchingPlayers.add(p);
			}
		}
		if (matchingPlayers.isEmpty()) {
			System.out.println("No matching players found.");
		} else if (matchingPlayers.size() > 1) {
			System.out.println("More than one matching player found.");
		} else {
			Player player = matchingPlayers.get(0);
			this.server.kickPlayer(player);
			System.out.println("Kicked player " + player.getName() + ".");
		}
	}
}
