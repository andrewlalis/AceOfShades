package nl.andrewlalis.aos_core.net.data;

import nl.andrewlalis.aos_core.model.Team;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TeamUpdate {
	public static final int BYTES = 1 + Integer.BYTES;

	private final byte id;
	private final int score;

	public TeamUpdate(Team team) {
		this.id = team.getId();
		this.score = team.getScore();
	}

	public TeamUpdate(byte id, int score) {
		this.id = id;
		this.score = score;
	}

	public byte getId() {
		return id;
	}

	public int getScore() {
		return score;
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeByte(this.id);
		out.writeInt(this.score);
	}

	public static TeamUpdate read(DataInputStream in) throws IOException {
		return new TeamUpdate(
			in.readByte(),
			in.readInt()
		);
	}
}
