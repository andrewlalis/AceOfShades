package nl.andrewlalis.aos_server.command;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.tools.GunType;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;
import nl.andrewlalis.aos_server.ClientHandler;
import nl.andrewlalis.aos_server.Server;
import nl.andrewlalis.aos_server.command.chat.ChatCommand;

import java.util.stream.Collectors;

public class GunsCommand implements Command, ChatCommand {
	private final Server server;

	public GunsCommand(Server server) {
		this.server = server;
	}

	@Override
	public void execute(String[] args) {
		for (var gunType : this.server.getWorld().getGunTypes().values()) {
			System.out.println(gunType.name());
		}
	}

	@Override
	public void execute(ClientHandler handler, Player player, String[] args) {
		String msg = handler.getServer().getWorld().getGunTypes().values().stream().map(GunType::name).collect(Collectors.joining(", "));
		handler.send(new SystemChatMessage(SystemChatMessage.Level.INFO, msg));
	}
}
