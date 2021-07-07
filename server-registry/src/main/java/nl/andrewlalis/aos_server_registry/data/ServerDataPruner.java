package nl.andrewlalis.aos_server_registry.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Scheduled task that runs once in a while and removes servers from the
 * registry which have not been updated in a while.
 */
public class ServerDataPruner implements Runnable {
	private static final Logger log = Logger.getLogger(ServerDataPruner.class.getName());

	private final long intervalMinutes;

	public ServerDataPruner(long intervalMinutes) {
		this.intervalMinutes = intervalMinutes;
	}

	@Override
	public void run() {
		try {
			var con = DataManager.getInstance().getConnection();
			String sql = """
					DELETE FROM servers
					WHERE DATEDIFF('MINUTE', servers.updated_at, CURRENT_TIMESTAMP(0)) > ?
					""";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setLong(1, this.intervalMinutes);
			int rowCount = stmt.executeUpdate();
			stmt.close();
			if (rowCount > 0) {
				log.info("Removed " + rowCount + " servers from registry due to inactivity.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
