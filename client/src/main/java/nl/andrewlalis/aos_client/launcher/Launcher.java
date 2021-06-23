package nl.andrewlalis.aos_client.launcher;

import nl.andrewlalis.aos_client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Launcher extends JFrame {
	private static final Pattern addressPattern = Pattern.compile("(.+):(\\d+)");

	public Launcher() throws HeadlessException {
		super("Ace of Shades - Launcher");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setContentPane(this.buildContent());
		this.pack();
		this.setLocationRelativeTo(null);
	}

	private Container buildContent() {
		JTabbedPane mainPanel = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		mainPanel.addTab("Connect", null, this.getConnectPanel(), "Connect to a server and play.");

		JPanel serversPanel = new JPanel();
		mainPanel.addTab("Servers", null, serversPanel, "View a list of available servers.");

		JPanel settingsPanel = new JPanel();
		mainPanel.addTab("Settings", null, settingsPanel, "Change game settings.");

		return mainPanel;
	}

	private Container getConnectPanel() {
		JPanel inputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);

		c.gridx = 0;
		c.gridy = 0;
		inputPanel.add(new JLabel("Address"), c);
		JTextField addressField = new JTextField(20);
		c.gridx = 1;
		inputPanel.add(addressField, c);

		c.gridy = 1;
		c.gridx = 0;
		inputPanel.add(new JLabel("Username"), c);
		JTextField usernameField = new JTextField(20);
		c.gridx = 1;
		inputPanel.add(usernameField, c);

		var enterListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (validateInput(addressField, usernameField)) {
						connect(addressField, usernameField);
					}
				}
			}
		};
		addressField.addKeyListener(enterListener);
		usernameField.addKeyListener(enterListener);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> this.dispose());
		JButton connectButton = new JButton("Connect");
		connectButton.addActionListener(e -> {
			if (validateInput(addressField, usernameField)) {
				connect(addressField, usernameField);
			}
		});
		buttonPanel.add(cancelButton);
		buttonPanel.add(connectButton);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(inputPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		return mainPanel;
	}

	private boolean validateInput(JTextField addressField, JTextField usernameField) {
		List<String> warnings = new ArrayList<>();
		if (addressField.getText() == null || addressField.getText().isBlank()) {
			warnings.add("Address must not be empty.");
		}
		if (usernameField.getText() == null || usernameField.getText().isBlank()) {
			warnings.add("Username must not be empty.");
		}
		if (usernameField.getText() != null && usernameField.getText().length() > 16) {
			warnings.add("Username is too long.");
		}
		if (addressField.getText() != null && !addressPattern.matcher(addressField.getText()).matches()) {
			warnings.add("Address must be in the form HOST:PORT.");
		}
		if (!warnings.isEmpty()) {
			JOptionPane.showMessageDialog(
				this,
				String.join("\n", warnings),
				"Invalid Input",
				JOptionPane.WARNING_MESSAGE
			);
		}
		return warnings.isEmpty();
	}

	private void connect(JTextField addressField, JTextField usernameField) {
		String hostAndPort = addressField.getText();
		String[] parts = hostAndPort.split(":");
		String host = parts[0].trim();
		int port = Integer.parseInt(parts[1]);
		String username = usernameField.getText();
		try {
			new Client(host, port, username);
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not connect:\n" + ex.getMessage(), "Connection Error", JOptionPane.WARNING_MESSAGE);
		}
	}


	public static void main(String[] args) {
		Launcher launcher = new Launcher();
		launcher.setVisible(true);
	}
}
