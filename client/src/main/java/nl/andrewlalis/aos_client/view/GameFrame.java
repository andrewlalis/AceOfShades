package nl.andrewlalis.aos_client.view;

import nl.andrewlalis.aos_client.Client;
import nl.andrewlalis.aos_client.control.PlayerKeyListener;
import nl.andrewlalis.aos_client.control.PlayerMouseListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

public class GameFrame extends JFrame {
	public GameFrame(String title, Client client, GamePanel gamePanel) throws HeadlessException {
		super(title);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		InputStream iconInputStream = GameFrame.class.getClassLoader().getResourceAsStream("/nl/andrewlalis/aos_client/icon.png");
		if (iconInputStream != null) {
			try {
				this.setIconImage(ImageIO.read(iconInputStream));
				iconInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.setResizable(false);

		gamePanel.setPreferredSize(new Dimension(800, 800));
		this.setContentPane(gamePanel);
		gamePanel.setFocusable(true);
		gamePanel.setRequestFocusEnabled(true);
		var mouseListener = new PlayerMouseListener(client, gamePanel);
		gamePanel.addKeyListener(new PlayerKeyListener(client));
		gamePanel.addMouseListener(mouseListener);
		gamePanel.addMouseMotionListener(mouseListener);
		gamePanel.addMouseWheelListener(mouseListener);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Closing...");
				client.shutdown();
			}
		});

		this.pack();

		gamePanel.requestFocusInWindow();
		this.setLocationRelativeTo(null);
	}
}
