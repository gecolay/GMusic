package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.model.PlaySettings;
import dev.geco.gmusic.model.PlayType;
import dev.geco.gmusic.model.Song;
import dev.geco.gmusic.model.PlayListMode;
import dev.geco.gmusic.model.PlayMode;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PlaySettingsService {

	private final GMusicMain gMusicMain;
	private final HashMap<UUID, PlaySettings> playSettingsCache = new HashMap<>();

	public PlaySettingsService(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	public void createDataTables() {
		try {
			gMusicMain.getDataService().execute("CREATE TABLE IF NOT EXISTS gmusic_play_setting (uuid CHAR(36) PRIMARY KEY, play_type INTEGER, play_list_mode INTEGER, volume INTEGER, play_mode INTEGER, show_particles INTEGER, reverse_mode INTEGER, toggle_mode INTEGER, range INTEGER);");
			gMusicMain.getDataService().execute("CREATE TABLE IF NOT EXISTS gmusic_play_setting_favorite (uuid CHAR(36), song_id TEXT, FOREIGN KEY (uuid) REFERENCES gmusic_play_setting(uuid) ON DELETE CASCADE ON UPDATE CASCADE);");
			migrateTo_2_4_0();
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not create play settings database tables!", e); }
	}

	private void migrateTo_2_4_0() throws SQLException {
		if(tableExists("gmusic_play_settings")) {
			try(ResultSet oldSettings = gMusicMain.getDataService().executeAndGet("SELECT uuid, playListMode, volume, playMode, showParticles, reverseMode, toggleMode, range FROM gmusic_play_settings")) {
				while(oldSettings.next()) {
					String uuid = oldSettings.getString("uuid");
					try(ResultSet rs = gMusicMain.getDataService().executeAndGet("SELECT 1 FROM gmusic_play_setting WHERE uuid = ? LIMIT 1", uuid)) {
						if(rs.next()) continue;
					}
					gMusicMain.getDataService().execute(
							"INSERT INTO gmusic_play_setting (uuid, play_type, play_list_mode, volume, play_mode, show_particles, reverse_mode, toggle_mode, range) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
							uuid,
							PlayType.DEFAULT.getId(),
							oldSettings.getInt("playListMode"),
							oldSettings.getInt("volume"),
							oldSettings.getInt("playMode"),
							oldSettings.getInt("showParticles"),
							oldSettings.getInt("reverseMode"),
							oldSettings.getInt("toggleMode"),
							oldSettings.getLong("range")
					);
				}
			}

			gMusicMain.getDataService().execute("DROP TABLE gmusic_play_settings");
		}

		if(tableExists("gmusic_play_settings_favorites")) {
			Map<String, List<String>> favoritesByUuid = new HashMap<>();
			try(ResultSet oldFavorites = gMusicMain.getDataService().executeAndGet("SELECT uuid, songId FROM gmusic_play_settings_favorites")) {
				while(oldFavorites.next()) {
					String uuid = oldFavorites.getString("uuid");
					String songId = oldFavorites.getString("songId");

					favoritesByUuid.computeIfAbsent(uuid, k -> new ArrayList<>()).add(songId);
				}
			}

			for(Map.Entry<String, List<String>> entry : favoritesByUuid.entrySet()) {
				String uuid = entry.getKey();
				List<String> songIds = entry.getValue();

				boolean uuidExistsInNewSettings;
				try(ResultSet rs = gMusicMain.getDataService().executeAndGet("SELECT 1 FROM gmusic_play_setting WHERE uuid = ? LIMIT 1", uuid)) {
					uuidExistsInNewSettings = rs.next();
				}

				if(!uuidExistsInNewSettings) {
					for(String songId : songIds) {
						gMusicMain.getDataService().execute(
								"INSERT INTO gmusic_play_setting_favorite (uuid, song_id) VALUES (?, ?)",
								uuid,
								songId
						);
					}
				}
			}

			gMusicMain.getDataService().execute("DROP TABLE gmusic_play_settings_favorites");
		}
	}

	/**
	 * @param tableName Unsafe for user input! Must be a <strong>constant</strong> table name
	 * @return if the table exists
	 */
	private boolean tableExists(String tableName) {
		try(ResultSet rs = gMusicMain.getDataService().executeAndGet("SELECT 1 FROM " + tableName + " LIMIT 1")) {
			return rs.next();
		} catch(Throwable ignored) { return false; }
	}

	public void loadPlaySettings() {
		playSettingsCache.clear();
		try {
			try(ResultSet playSettingsData = gMusicMain.getDataService().executeAndGet("SELECT * FROM gmusic_play_setting")) {
				while(playSettingsData.next()) {
					UUID uuid = UUID.fromString(playSettingsData.getString("uuid"));
					PlayType playType = PlayType.byId(playSettingsData.getInt("play_type"));
					if(playType == PlayType.DEFAULT && Bukkit.getPlayer(uuid) == null) continue;

					playSettingsCache.put(uuid, new PlaySettings(
							uuid,
							playType,
							PlayListMode.byId(playSettingsData.getInt("play_list_mode")),
							playSettingsData.getInt("volume"),
							PlayMode.byId(playSettingsData.getInt("play_mode")),
							playSettingsData.getBoolean("show_particles"),
							playSettingsData.getBoolean("reverse_mode"),
							playSettingsData.getBoolean("toggle_mode"),
							playSettingsData.getLong("range"),
							new ArrayList<>()
					));
				}
			}

			try(ResultSet playSettingsFavoritesData = gMusicMain.getDataService().executeAndGet("SELECT * FROM gmusic_play_setting_favorite")) {
				while(playSettingsFavoritesData.next()) {
					UUID uuid = UUID.fromString(playSettingsFavoritesData.getString("uuid"));
					PlaySettings playSettings = playSettingsCache.get(uuid);
					if(playSettings == null) continue;

					Song song = gMusicMain.getSongService().getSongById(playSettingsFavoritesData.getString("song_id"));
					if(song == null) continue;

					playSettings.addFavoriteSong(song);
				}
			}
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not load play states", e); }
	}

	public @NotNull PlaySettings getPlaySettings(@NotNull UUID uuid, @NotNull PlayType playType) {
		if(playSettingsCache.containsKey(uuid)) return playSettingsCache.get(uuid);

		List<Song> favorites = new ArrayList<>();

		PlaySettings playSettings = null;

		try {
			try(ResultSet playSettingsFavoritesData = gMusicMain.getDataService().executeAndGet("SELECT song_id FROM gmusic_play_setting_favorite WHERE uuid = ?", uuid.toString())) {
				while(playSettingsFavoritesData.next()) {
					favorites.add(gMusicMain.getSongService().getSongById(playSettingsFavoritesData.getString("song_id")));
				}
			}

			try(ResultSet playSettingsData = gMusicMain.getDataService().executeAndGet("SELECT * FROM gmusic_play_setting WHERE uuid = ?", uuid.toString())) {
				if(playSettingsData.next()) {
					playSettings = new PlaySettings(
							uuid,
							PlayType.byId(playSettingsData.getInt("play_type")),
							PlayListMode.byId(playSettingsData.getInt("play_list_mode")),
							playSettingsData.getInt("volume"),
							PlayMode.byId(playSettingsData.getInt("play_mode")),
							playSettingsData.getBoolean("show_particles"),
							playSettingsData.getBoolean("reverse_mode"),
							playSettingsData.getBoolean("toggle_mode"),
							playSettingsData.getLong("range"),
							favorites
					);
				}
			}
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not load play settings", e); }

		if(playSettings != null) playSettingsCache.put(uuid, playSettings);
		else playSettings = generateDefaultPlaySettings(uuid, playType);

		playSettings.setFavorites(favorites);

		return playSettings;
	}

	public @NotNull PlaySettings generateDefaultPlaySettings(@NotNull UUID uuid, @NotNull PlayType playType) {
		PlaySettings playSettings = new PlaySettings(
				uuid,
				playType,
				PlayListMode.byId(gMusicMain.getConfigService().PS_D_PLAYLIST_MODE),
				gMusicMain.getConfigService().PS_D_VOLUME,
				PlayMode.byId(gMusicMain.getConfigService().PS_D_PLAY_MODE),
				gMusicMain.getConfigService().PS_D_PARTICLES,
				gMusicMain.getConfigService().PS_D_REVERSE,
				false,
				0,
				new ArrayList<>()
		);

		playSettingsCache.put(uuid, playSettings);

		return playSettings;
	}

	public void savePlaySettings(@NotNull UUID uuid, @NotNull PlaySettings playSettings) {
		try {
			gMusicMain.getDataService().execute("DELETE FROM gmusic_play_setting_favorite WHERE uuid = ?", uuid.toString());

			String sql = switch(gMusicMain.getDataService().getType()) {
				case "sqlite" -> """
				INSERT INTO gmusic_play_setting (
					uuid,
					play_type,
					play_list_mode,
					volume,
					play_mode,
					show_particles,
					reverse_mode,
					toggle_mode,
					range
				)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
				ON CONFLICT(uuid) DO UPDATE SET
					play_type      = excluded.play_type,
					play_list_mode = excluded.play_list_mode,
					volume         = excluded.volume,
					play_mode      = excluded.play_mode,
					show_particles = excluded.show_particles,
					reverse_mode   = excluded.reverse_mode,
					toggle_mode    = excluded.toggle_mode,
					range          = excluded.range
				""";

				default -> """
				INSERT INTO gmusic_play_setting (
					uuid,
					play_type,
					play_list_mode,
					volume,
					play_mode,
					show_particles,
					reverse_mode,
					toggle_mode,
					range
				)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) AS new
				ON DUPLICATE KEY UPDATE
					play_type      = new.play_type,
					play_list_mode = new.play_list_mode,
					volume         = new.volume,
					play_mode      = new.play_mode,
					show_particles = new.show_particles,
					reverse_mode   = new.reverse_mode,
					toggle_mode    = new.toggle_mode,
					range          = new.range
				""";
			};

			gMusicMain.getDataService().execute(
					sql,
					uuid.toString(),
					playSettings.getPlayType().getId(),
					playSettings.getPlayListMode().getId(),
					playSettings.getVolume(),
					playSettings.getPlayMode().getId(),
					playSettings.isShowingParticles(),
					playSettings.isReverseMode(),
					playSettings.isToggleMode(),
					playSettings.getRange()
			);

			if(playSettings.getFavorites().isEmpty()) return;

			for(Song favoriteSong : playSettings.getFavorites()) {
				gMusicMain.getDataService().execute("INSERT INTO gmusic_play_setting_favorite (uuid, song_id) VALUES (?, ?)", uuid.toString(), favoriteSong.getId());
			}
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not save play settings", e); }
	}

	public void removePlaySettings(@NotNull UUID uuid) {
		try {
			removePlaySettingsCache(uuid);
			gMusicMain.getDataService().execute("DELETE FROM gmusic_play_setting WHERE uuid = ?", uuid.toString());
			gMusicMain.getDataService().execute("DELETE FROM gmusic_play_setting_favorite WHERE uuid = ?", uuid.toString());
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not remove play settings", e); }
	}

	public void savePlaySettings() {
		for(Map.Entry<UUID, PlaySettings> playSettings : playSettingsCache.entrySet()) {
			savePlaySettings(playSettings.getKey(), playSettings.getValue());
		}

		playSettingsCache.clear();
	}

	public void removePlaySettingsCache(@NotNull UUID uuid) { playSettingsCache.remove(uuid); }

}