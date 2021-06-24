package nl.andrewlalis.aos_client.launcher.servers;

import java.awt.*;

public class EditServerDialog extends AddServerDialog {
	public EditServerDialog(Frame owner, ServerInfo server) {
		super(owner);
		this.setTitle("Edit Server - " + server.getName());
		this.serverNameField.setText(server.getName());
		this.serverAddressField.setText(server.getHost());
		this.usernameField.setText(server.getUsername());
	}
}
