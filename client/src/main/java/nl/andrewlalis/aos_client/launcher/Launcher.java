package nl.andrewlalis.aos_client.launcher;

import nl.andrewlalis.aos_client.Client;
import nl.andrewlalis.aos_client.launcher.servers.ServerInfo;
import nl.andrewlalis.aos_client.launcher.servers.ServerInfoCellRenderer;
import nl.andrewlalis.aos_client.launcher.servers.ServerInfoListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Launcher application for starting the game. Because the client is only
 * actually started when connecting to a server, this user interface serves as
 * the menu that the user interacts with before joining a game.
 */
public class Launcher extends JFrame {
	private static final Pattern addressPattern = Pattern.compile("(.+):(\\d+)");

	public Launcher() throws HeadlessException {
		super("Ace of Shades - Launcher");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setContentPane(this.buildContent());
		this.setPreferredSize(new Dimension(400, 500));
		this.pack();
		this.setLocationRelativeTo(null);
	}

	private Container buildContent() {
		JTabbedPane mainPanel = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		mainPanel.addTab("Servers", null, this.getServersPanel(), "View a list of available servers.");
//
//		JPanel settingsPanel = new JPanel();
//		mainPanel.addTab("Settings", null, settingsPanel, "Change game settings.");

		return mainPanel;
	}

	private Container getServersPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		ServerInfoListModel listModel = new ServerInfoListModel();
		listModel.add(new ServerInfo("one", "localhost:8035", "andrew"));
		listModel.add(new ServerInfo("two", "localhost:25565", null));
		JList<ServerInfo> serversList = new JList<>(listModel);
		serversList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		serversList.setCellRenderer(new ServerInfoCellRenderer());
		JScrollPane scrollPane = new JScrollPane(serversList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane, BorderLayout.CENTER);

		serversList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					serversList.setSelectedIndex(serversList.locationToIndex(e.getPoint()));
					ServerInfo server = serversList.getSelectedValue();
					if (server == null) return;
					JPopupMenu menu = new JPopupMenu();
					JMenuItem connectItem = new JMenuItem("Connect");
					connectItem.addActionListener(a -> connect(server));
					menu.add(connectItem);
					JMenuItem editItem = new JMenuItem("Edit");
					editItem.addActionListener(a -> {
						// TODO: Open edit dialog.
						listModel.serverEdited();
					});
					menu.add(editItem);
					JMenuItem removeItem = new JMenuItem("Remove");
					removeItem.addActionListener(a -> {
						int choice = JOptionPane.showConfirmDialog(panel, "Are you sure you want to remove this server?", "Confirm Server Removal", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
						if (choice == JOptionPane.OK_OPTION) {
							listModel.remove(serversList.getSelectedValue());
						}
					});
					menu.add(removeItem);
					menu.show(serversList, e.getX(), e.getY());
				} else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					serversList.setSelectedIndex(serversList.locationToIndex(e.getPoint()));
					ServerInfo server = serversList.getSelectedValue();
					if (server == null) return;
					connect(server);
				}
			}
		});

		JPanel buttonPanel = new JPanel();
		JButton addServerButton = new JButton("Add Server");
		addServerButton.setToolTipText("Add a new server to the list.");
		addServerButton.addActionListener(e -> {
			// TODO: Add server dialog.
		});
		buttonPanel.add(addServerButton);
		JButton directConnectButton = new JButton("Direct Connect");
		directConnectButton.setToolTipText("Connect to any server directly.");
		directConnectButton.addActionListener(e -> {
			JDialog dialog = new JDialog(this, true);
			dialog.setTitle("Direct Connect");
			dialog.setContentPane(getConnectPanel(dialog));
			dialog.pack();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		});
		buttonPanel.add(directConnectButton);

		JButton helpButton = new JButton("Help");
		helpButton.setToolTipText("Show some helpful information for using this program.");
		helpButton.addActionListener(e -> {
			String uri = "https://github.com/andrewlalis/AceOfShades/blob/main/help.md";
			try {
				Desktop.getDesktop().browse(new URI(uri));
			} catch (IOException | URISyntaxException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "Could not open URI in browser. For help, please visit\n" + uri, "Error", JOptionPane.WARNING_MESSAGE);
			}
		});
		buttonPanel.add(helpButton);

		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private Container getConnectPanel(JDialog dialog) {
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

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton connectButton = new JButton("Connect");
		connectButton.addActionListener(e -> {
			if (validateConnectInput(addressField, usernameField)) {
				dialog.dispose();
				connect(new ServerInfo("Unknown", addressField.getText(), usernameField.getText()));
			}
		});
		buttonPanel.add(connectButton);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(inputPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		return mainPanel;
	}

	private boolean validateConnectInput(JTextField addressField, JTextField usernameField) {
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

	private void connect(ServerInfo serverInfo) {
		String username = serverInfo.getUsername();
		if (username == null) {
			username = JOptionPane.showInputDialog(this, "Enter a username.", "Username", JOptionPane.PLAIN_MESSAGE);
			if (username == null || username.isBlank()) return;
		}
		try {
			new Client(serverInfo.getHostAddress(), serverInfo.getHostPort(), username);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Could not connect:\n" + e.getMessage(), "Connection Error", JOptionPane.WARNING_MESSAGE);
		}
	}


	public static void main(String[] args) {
		Launcher launcher = new Launcher();
		launcher.setVisible(true);
	}
}
