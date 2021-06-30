package nl.andrewlalis.aos_client.launcher.servers;

import nl.andrewlalis.aos_client.launcher.Launcher;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AddServerDialog extends JDialog {
	protected JTextField serverNameField;
	protected JTextField serverAddressField;
	protected JTextField usernameField;

	private ServerInfo serverInfo;

	public AddServerDialog(Frame owner) {
		super(owner, true);
		this.setTitle("Add Server");
		this.setContentPane(this.getContent());
		this.pack();
		this.setLocationRelativeTo(owner);
	}

	public ServerInfo getServerInfo() {
		return this.serverInfo;
	}

	private Container getContent() {
		JPanel container = new JPanel(new BorderLayout());
		JPanel inputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);

		c.gridx = 0;
		c.gridy = 0;
		inputPanel.add(new JLabel("Name"), c);
		serverNameField = new JTextField(20);
		c.gridx++;
		inputPanel.add(serverNameField, c);

		c.gridx = 0;
		c.gridy++;
		inputPanel.add(new JLabel("Address"), c);
		serverAddressField = new JTextField(20);
		c.gridx++;
		inputPanel.add(serverAddressField, c);

		c.gridy++;
		c.gridx = 0;
		inputPanel.add(new JLabel("Username"), c);
		usernameField = new JTextField(20);
		c.gridx++;
		inputPanel.add(usernameField, c);

		container.add(inputPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> {
			this.serverInfo = null;
			this.dispose();
		});
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(e -> {
			var messages = this.validateInputs();
			if (!messages.isEmpty()) {
				String msg = String.join("\n", messages);
				JOptionPane.showMessageDialog(this, "The information you entered is not valid:\n" + msg, "Invalid Server Data", JOptionPane.WARNING_MESSAGE);
			} else {
				String username = this.usernameField.getText();
				if (username.isBlank()) {
					username = null;
				}
				this.serverInfo = new ServerInfo(this.serverNameField.getText(), this.serverAddressField.getText(), username);
				this.dispose();
			}
		});
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		container.add(buttonPanel, BorderLayout.SOUTH);

		return container;
	}

	private List<String> validateInputs() {
		String name = this.serverNameField.getText();
		String address = this.serverAddressField.getText();
		String username = this.usernameField.getText();
		List<String> messages = new ArrayList<>();

		if (name == null || name.isBlank()) {
			messages.add("Server name cannot be blank.");
		}
		if (name != null && name.length() > 32) {
			messages.add("Server name is too long.");
		}
		if (address == null || address.isBlank()) {
			messages.add("Server address cannot be blank.");
		}
		if (address != null && !Launcher.addressPattern.matcher(address).matches()) {
			messages.add("Server address is not properly formatted as HOST:PORT.");
		}
		if (username != null && !username.isBlank() && username.length() > 16) {
			messages.add("Username is too long. Maximum of 16 characters.");
		}
		if (username != null && !username.isBlank() && !Launcher.usernamePattern.matcher(username).matches()) {
			messages.add("Username should contain only letters, numbers, underscores, and hyphens.");
		}
		return messages;
	}
}
