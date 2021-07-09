package nl.andrewlalis.aos_core.net.data;

import nl.andrewlalis.aos_core.model.Bullet;
import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.Team;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The minimal data that's sent to each client after every game tick. This
 * contains the most basic information on updates to bullets, object movement,
 * and sounds that need to be played, and other simple things.
 * <p>
 *     This update doesn't contain all data about players and the world, and
 *     this extra data is sent periodically to keep clients up-to-date without
 *     sending too much data.
 * </p>
 */
public class WorldUpdate {
	private final List<PlayerUpdate> playerUpdates;
	private final List<BulletUpdate> bulletUpdates;
	private final List<TeamUpdate> teamUpdates;
	private final List<SoundData> soundsToPlay;

	public WorldUpdate() {
		this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}

	private WorldUpdate(List<PlayerUpdate> playerUpdates, List<BulletUpdate> bulletUpdates, List<TeamUpdate> teamUpdates, List<SoundData> soundsToPlay) {
		this.playerUpdates = playerUpdates;
		this.bulletUpdates = bulletUpdates;
		this.teamUpdates = teamUpdates;
		this.soundsToPlay = soundsToPlay;
	}

	public void clear() {
		this.playerUpdates.clear();
		this.bulletUpdates.clear();
		this.teamUpdates.clear();
		this.soundsToPlay.clear();
	}

	public void addPlayer(Player p) {
		this.playerUpdates.add(new PlayerUpdate(p));
	}

	public void addBullet(Bullet b) {
		this.bulletUpdates.add(new BulletUpdate(b));
	}

	public void addTeam(Team team) {
		this.teamUpdates.add(new TeamUpdate(team));
	}

	public void addSound(SoundData sound) {
		this.soundsToPlay.add(sound);
	}

	public List<PlayerUpdate> getPlayerUpdates() {
		return playerUpdates;
	}

	public List<BulletUpdate> getBulletUpdates() {
		return bulletUpdates;
	}

	public List<TeamUpdate> getTeamUpdates() {
		return teamUpdates;
	}

	public List<SoundData> getSoundsToPlay() {
		return soundsToPlay;
	}

	public byte[] toBytes() throws IOException {
		int size = 3 * Integer.BYTES + // List size integers.
			this.playerUpdates.size() * PlayerUpdate.BYTES +
			this.bulletUpdates.size() * BulletUpdate.BYTES +
			this.teamUpdates.size() * TeamUpdate.BYTES +
			this.soundsToPlay.size() * SoundData.BYTES;
		ByteArrayOutputStream out = new ByteArrayOutputStream(size);
		DataOutputStream dataOut = new DataOutputStream(out);
		dataOut.writeInt(this.playerUpdates.size());
		for (var u : this.playerUpdates) {
			u.write(dataOut);
		}
		dataOut.writeInt(this.bulletUpdates.size());
		for (var u : this.bulletUpdates) {
			u.write(dataOut);
		}
		dataOut.writeInt(this.teamUpdates.size());
		for (var u : this.teamUpdates) {
			u.write(dataOut);
		}
		dataOut.writeInt(this.soundsToPlay.size());
		for (var u : this.soundsToPlay) {
			u.write(dataOut);
		}
		byte[] data = out.toByteArray();
		dataOut.close();
		return data;
	}

	public static WorldUpdate fromBytes(byte[] data) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		DataInputStream dataIn = new DataInputStream(in);

		int players = dataIn.readInt();
		List<PlayerUpdate> playerUpdates = new ArrayList<>(players);
		for (int i = 0; i < players; i++) {
			playerUpdates.add(PlayerUpdate.read(dataIn));
		}
		int bullets = dataIn.readInt();
		List<BulletUpdate> bulletUpdates = new ArrayList<>(bullets);
		for (int i = 0; i < bullets; i++) {
			bulletUpdates.add(BulletUpdate.read(dataIn));
		}
		int teams = dataIn.readInt();
		List<TeamUpdate> teamUpdates = new ArrayList<>(teams);
		for (int i = 0; i < teams; i++) {
			teamUpdates.add(TeamUpdate.read(dataIn));
		}
		int sounds = dataIn.readInt();
		List<SoundData> soundsToPlay = new ArrayList<>(sounds);
		for (int i = 0; i < sounds; i++) {
			soundsToPlay.add(SoundData.read(dataIn));
		}
		var obj = new WorldUpdate(playerUpdates, bulletUpdates, teamUpdates, soundsToPlay);
		dataIn.close();
		return obj;
	}
}
