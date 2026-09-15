package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.model.PlayListMode;
import dev.geco.gmusic.model.PlayMode;
import dev.geco.gmusic.model.PlayType;
import dev.geco.gmusic.model.gui.MusicGUI;
import dev.geco.gmusic.model.NotePart;
import dev.geco.gmusic.model.PlaySettings;
import dev.geco.gmusic.model.Song;
import dev.geco.gmusic.model.PlayState;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;

public class PlayService {

	private final GMusicMain gMusicMain;
	private final Random random = new Random();
	private final HashMap<UUID, PlayState> playStateCache = new HashMap<>();

	public PlayService(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	public void createDataTables() {
		try {
			gMusicMain.getDataService().execute("CREATE TABLE IF NOT EXISTS gmusic_play_state (uuid CHAR(36) PRIMARY KEY, play_type TEXT, song_id TEXT, tick INTEGER, paused INTEGER);");
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not create play settings database tables!", e); }
	}

	public void loadPlayStates() {
		playStateCache.clear();
		List<String> uuidsToDelete = new ArrayList<>();
		try {
			try(ResultSet playStateData = gMusicMain.getDataService().executeAndGet("SELECT * FROM gmusic_play_state")) {
				while(playStateData.next()) {
					UUID uuid = UUID.fromString(playStateData.getString("uuid"));

					Song song = gMusicMain.getSongService().getSongById(playStateData.getString("song_id"));
					if(song == null) {
						uuidsToDelete.add(uuid.toString());
						continue;
					}

					PlayState playState = new PlayState(
							uuid,
							PlayType.byId(playStateData.getInt("play_type")),
							song,
							new Timer(),
							playStateData.getLong("tick")
					);

					playState.setPaused(playStateData.getBoolean("paused"));

					if(playState.getPlayType() == PlayType.DEFAULT) {
						Player player = Bukkit.getPlayer(uuid);
						if(player == null) continue;
						playStateCache.put(uuid, playState);
						if(!playState.isPaused()) gMusicMain.getPlayService().playSong(player, playState.getSong(), -playState.getTickPosition());
						uuidsToDelete.add(uuid.toString());
						continue;
					}

					playStateCache.put(uuid, playState);
					uuidsToDelete.add(uuid.toString());
				}
			}

			if(!uuidsToDelete.isEmpty()) {
				String placeholders = String.join(",", Collections.nCopies(uuidsToDelete.size(), "?"));
				gMusicMain.getDataService().execute("DELETE FROM gmusic_play_state WHERE uuid IN (" + placeholders + ");", uuidsToDelete.toArray());
			}
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not load play states", e); }
	}

	public void playSong(@NotNull Player player, @Nullable Song song) { playSong(player, song, 0); }

	public void playSong(@NotNull Player player, @Nullable Song song, long delay) {
		if(song == null) return;

		PlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId(), PlayType.DEFAULT);
		if(playSettings.getPlayListMode() == PlayListMode.RADIO) return;

		PlayState playState = getPlayState(player.getUniqueId());
		if(playState != null) playState.getTimer().cancel();

		Timer timer = new Timer();
		playState = new PlayState(player.getUniqueId(), PlayType.DEFAULT, song, timer, playSettings.isReverseMode() ? song.getLength() + delay : -delay);
		setPlayState(player.getUniqueId(), playState);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) {
			gMusicMain.getMessageService().sendActionBarMessage(
					player,
					"Messages.actionbar-play",
					"%Song%", song.getId(),
					"%SongTitle%", song.getTitle(),
					"%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(),
					"%OriginalAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-original-author") : song.getOriginalAuthor()
			);
		}

		startSong(player, song, timer);
	}

	public @Nullable Song getRandomSong(@NotNull UUID uuid, PlayType playType) {
		PlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid, playType);
		List<Song> songs = playSettings.getPlayListMode() == PlayListMode.FAVORITES ? playSettings.getFavorites() : gMusicMain.getSongService().getSongs();
		return !songs.isEmpty() ? songs.get(random.nextInt(songs.size())) : null;
	}

	public @Nullable Song getShuffleSong(@NotNull UUID uuid, @NotNull Song song, PlayType playType) {
		PlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid, playType);
		List<Song> songs = playSettings.getPlayListMode() == PlayListMode.FAVORITES ? playSettings.getFavorites() : gMusicMain.getSongService().getSongs();
		return !songs.isEmpty() ? songs.indexOf(song) + 1 == songs.size() ? songs.get(0) : songs.get(songs.indexOf(song) + 1) : null;
	}

	private void startSong(@NotNull Player player, @NotNull Song song, @NotNull Timer timer) {
		UUID uuid = player.getUniqueId();
		PlayState playState = getPlayState(uuid);
		PlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId(), PlayType.DEFAULT);

		final long[] ticker = {0};

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				ticker[0]++;

				long position = playState.getTickPosition();

				List<NotePart> noteParts = song.getContent().get(position);

				if(noteParts != null && playSettings.getVolume() > 0) {
					if(playSettings.isShowingParticles()) player.spawnParticle(Particle.NOTE, player.getEyeLocation().add(random.nextDouble() - 0.5, 0.3, random.nextDouble() - 0.5), 0, random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);

					for(NotePart notePart : noteParts) {
						gMusicMain.getMusicUtil().playAtPlayer(player, notePart, playSettings);
					}
				}

				if(position == (playSettings.isReverseMode() ? 0 : song.getLength())) {
					if(playSettings.getPlayMode() == PlayMode.LOOP) {
						position = playSettings.isReverseMode() ? song.getLength() + gMusicMain.getConfigService().PS_TIME_UNTIL_REPEAT : -gMusicMain.getConfigService().PS_TIME_UNTIL_REPEAT;
						playState.setTickPosition(position);
					} else {
						timer.cancel();

						if(playSettings.getPlayMode() == PlayMode.SHUFFLE) playSong(player, getShuffleSong(uuid, song, PlayType.DEFAULT), gMusicMain.getConfigService().PS_TIME_UNTIL_SHUFFLE);
						else {
							playStateCache.remove(uuid);
							MusicGUI musicGUI = MusicGUI.getMusicGUI(uuid);
							if(musicGUI != null) musicGUI.setPauseResumeBar();
						}
					}
					return;
				}

				playState.setTickPosition(playSettings.isReverseMode() ? position - 1 : position + 1);

				if(gMusicMain.getConfigService().A_SHOW_WHILE_PLAYING && ticker[0] % 2000 == 0) {
					gMusicMain.getMessageService().sendActionBarMessage(
							player,
							"Messages.actionbar-playing",
							"%Song%", song.getId(),
							"%SongTitle%", song.getTitle(),
							"%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(),
							"%OriginalAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-original-author") : song.getOriginalAuthor()
					);
				}
			}
		}, 0, 1);
	}

	public @Nullable PlayState getPlayState(@NotNull UUID uuid) {
		if(playStateCache.containsKey(uuid)) return playStateCache.get(uuid);

		PlayState playState = null;

		try {
			try(ResultSet playStateData = gMusicMain.getDataService().executeAndGet("SELECT * FROM gmusic_play_state WHERE uuid = ?", uuid.toString())) {
				while(playStateData.next()) {
					Song song = gMusicMain.getSongService().getSongById(playStateData.getString("song_id"));
					if(song == null) {
						gMusicMain.getDataService().execute("DELETE FROM gmusic_play_state WHERE uuid = ?;", uuid.toString());
						continue;
					}

					playState = new PlayState(
							uuid,
							PlayType.byId(playStateData.getInt("play_type")),
							song,
							new Timer(),
							playStateData.getLong("tick")
					);

					gMusicMain.getDataService().execute("DELETE FROM gmusic_play_state WHERE uuid = ?;", uuid.toString());
				}
			}
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not load play state", e); }

		playStateCache.put(uuid, playState);

		return playState;
	}

	public void clearPlayState(@NotNull UUID uuid) { playStateCache.put(uuid, null); }

	public void removePlayState(@NotNull UUID uuid) { playStateCache.remove(uuid); }

	public void setPlayState(@NotNull UUID uuid, @NotNull PlayState playState) { playStateCache.put(uuid, playState); }

	public boolean hasPlayingSong(@NotNull UUID uuid) { return getPlayState(uuid) != null; }

	public boolean hasPausedSong(@NotNull UUID uuid) {
		PlayState playState = getPlayState(uuid);
		return playState != null && playState.isPaused();
	}

	public @Nullable Song getPlayingSong(@NotNull UUID uuid) {
		PlayState playState = getPlayState(uuid);
		return playState != null ? playState.getSong() : null;
	}

	public @Nullable Song getNextSong(@NotNull Player player) {
		PlayState playState = getPlayState(player.getUniqueId());
		return playState != null ? getShuffleSong(player.getUniqueId(), playState.getSong(), PlayType.DEFAULT) : getRandomSong(player.getUniqueId(), PlayType.DEFAULT);
	}

	public void savePlayState(@NotNull UUID uuid, @NotNull PlayState playState) {
		try {
			String sql = switch(gMusicMain.getDataService().getType()) {
				case "sqlite" -> """
                INSERT INTO gmusic_play_state (
                    uuid,
                    play_type,
                    song_id,
                    tick,
                    paused
                )
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    play_type = excluded.play_type,
                    song_id   = excluded.song_id,
                    tick      = excluded.tick,
                    paused    = excluded.paused
                """;

				default -> """
                INSERT INTO gmusic_play_state (
                    uuid,
                    play_type,
                    song_id,
                    tick,
                    paused
                )
                VALUES (?, ?, ?, ?, ?) AS new
                ON DUPLICATE KEY UPDATE
                    play_type = new.play_type,
                    song_id   = new.song_id,
                    tick      = new.tick,
                    paused    = new.paused
                """;
			};

			gMusicMain.getDataService().execute(
					sql,
					uuid.toString(),
					playState.getPlayType().getId(),
					playState.getSong().getId(),
					playState.getTickPosition(),
					playState.isPaused()
			);
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not save play state", e); }
	}

	public void savePlayStates() {
		for(Map.Entry<UUID, PlayState> playState : playStateCache.entrySet()) {
			if(playState.getValue() == null) continue;

			playState.getValue().getTimer().cancel();

			savePlayState(playState.getKey(), playState.getValue());

			Player player = Bukkit.getPlayer(playState.getKey());
			if(player != null && gMusicMain.getConfigService().A_SHOW_MESSAGES) gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-stop");
		}

		playStateCache.clear();
	}

	public void stopSong(@NotNull Player player) {
		PlayState playState = getPlayState(player.getUniqueId());
		if(playState == null) return;

		playState.getTimer().cancel();

		playStateCache.remove(player.getUniqueId());

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-stop");
	}

	public void pauseSong(@NotNull Player player) {
		PlayState playState = getPlayState(player.getUniqueId());
		if(playState == null) return;

		playState.getTimer().cancel();
		playState.setPaused(true);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-pause");
	}

	public void resumeSong(@NotNull Player player) {
		PlayState playState = getPlayState(player.getUniqueId());
		if(playState == null) return;

		playState.setTimer(new Timer());
		playState.setPaused(false);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-resume");

		startSong(player, playState.getSong(), playState.getTimer());
	}

}