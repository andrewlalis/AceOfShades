package nl.andrewlalis.aos_server.command;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_server.Server;

import java.util.stream.Collectors;

public class ListPlayersCommand implements Command {
	private final Server server;

	public ListPlayersCommand(Server server) {
		this.server = server;
	}

	@Override
	public void execute(String[] args) {
		if (this.server.getWorld().getPlayers().isEmpty()) {
			System.out.println("There are no players currently online.");
			return;
		}
		String message = this.server.getWorld().getPlayers().values().stream()
			.sorted()
			.map(player -> String.format(
				"%d | %s   Team: %s, Health: %.1f / %.1f, Gun: %s",
				player.getId(),
				player.getName(),
				player.getTeam() == null ? "none" : player.getTeam().getName(),
				player.getHealth(),
				Player.MAX_HEALTH,
				player.getGun().getType().getName()
			))
			.collect(Collectors.joining("\n"));
		System.out.println(message);
	}
}
