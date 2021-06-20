package nl.andrewlalis.aos_server.command;

import java.io.IOException;
import java.io.InputStream;

public class HelpCommand implements Command {
	@Override
	public void execute(String[] args) {
		try {
			InputStream is = HelpCommand.class.getClassLoader().getResourceAsStream("help.txt");
			if (is == null) throw new IOException("Could not load help.txt.");
			String helpMessage = new String(is.readAllBytes());
			System.out.println(helpMessage);
		} catch (IOException e) {
			System.err.println("Could not load help information: " + e.getMessage());
		}
	}
}
