package nl.andrewlalis.aos_server_registry.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Scheduled task that runs once in a while and removes servers from the
 * registry which have not been updated in a while.
 */
public class ServerDataPruner implements Runnable {
	public static final int INTERVAL_MINUTES = 5;
	@Override
	public void run() {
		try {
			var con = DataManager.getInstance().getConnection();
			String sql = """
					DELETE FROM servers
					WHERE DATEDIFF('MINUTE', servers.updated_at, CURRENT_TIMESTAMP(0)) > ?
					""";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setInt(1, INTERVAL_MINUTES);
			int rowCount = stmt.executeUpdate();
			stmt.close();
			if (rowCount > 0) {
				System.out.println("Removed " + rowCount + " servers from registry due to inactivity.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
