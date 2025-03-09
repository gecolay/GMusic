package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.object.GPlaySettings;
import dev.geco.gmusic.object.GSong;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PlaySettingsService {

	private final GMusicMain gMusicMain;
	private final HashMap<UUID, GPlaySettings> playSettingsCache = new HashMap<>();

	public PlaySettingsService(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	public void createTable() {
		try {
			gMusicMain.getDataService().execute("CREATE TABLE IF NOT EXISTS gmusic_play_settings (uuid TEXT, playList INTEGER, volume INTEGER, playOnJoin INTEGER, playMode INTEGER, showParticles INTEGER, reverseMode INTEGER, toggleMode INTEGER, range INTEGER, currentSong TEXT);");
			gMusicMain.getDataService().execute("CREATE TABLE IF NOT EXISTS gmusic_play_settings_favorites (uuid TEXT, songId TEXT);");
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not create database tables!", e); }
	}

	public GPlaySettings getPlaySettings(UUID uuid) {
		if(playSettingsCache.containsKey(uuid)) return playSettingsCache.get(uuid);

		List<GSong> favorites = new ArrayList<>();

		GPlaySettings playSettings = null;

		try {
			ResultSet playSettingsFavoritesData = gMusicMain.getDataService().executeAndGet("SELECT * FROM gmusic_play_settings_favorites WHERE uuid = ?", uuid.toString());

			while (playSettingsFavoritesData.next()) {
				favorites.add(gMusicMain.getSongService().getSongById(playSettingsFavoritesData.getString("songId")));
			}

			ResultSet playSettingsData = gMusicMain.getDataService().executeAndGet("SELECT * FROM gmusic_play_settings WHERE uuid = ?", uuid.toString());
			if(playSettingsData.next()) {
				playSettings = new GPlaySettings(uuid, playSettingsData.getInt("playList"), playSettingsData.getLong("volume"), playSettingsData.getBoolean("playOnJoin"), playSettingsData.getInt("playMode"), playSettingsData.getBoolean("showParticles"), playSettingsData.getBoolean("reverseMode"), playSettingsData.getBoolean("toggleMode"), playSettingsData.getLong("range"), playSettingsData.getString("currentSong"), favorites);
			}
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not load play settings", e); }

		if(playSettings == null) playSettings = generateDefaultPlaySettings(uuid);

		playSettings.setFavorites(favorites);

		playSettingsCache.put(uuid, playSettings);

		return playSettings;
	}

	private GPlaySettings generateDefaultPlaySettings(UUID UUID) {
		return new GPlaySettings(UUID, gMusicMain.getConfigService().PS_D_PLAYLIST, gMusicMain.getConfigService().PS_D_VOLUME, gMusicMain.getConfigService().R_PLAY_ON_JOIN, gMusicMain.getConfigService().PS_D_PLAY_MODE, gMusicMain.getConfigService().PS_D_PARTICLES, gMusicMain.getConfigService().PS_D_REVERSE, false, 0, null, new ArrayList<>());
	}

	public void setPlaySettings(UUID uuid, GPlaySettings playSettings) {
		try {
			gMusicMain.getDataService().execute("DELETE FROM gmusic_play_settings WHERE uuid = ?", uuid.toString());
			gMusicMain.getDataService().execute("DELETE FROM gmusic_play_settings_favorites WHERE uuid = ?", uuid.toString());

			if(playSettings == null) {
				playSettingsCache.remove(uuid);
				return;
			}

			playSettingsCache.put(uuid, playSettings);

			gMusicMain.getDataService().execute("INSERT INTO gmusic_play_settings (uuid, playList, volume, playOnJoin, playMode, showParticles, reverseMode, toggleMode, range, currentSong) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					uuid.toString(),
					playSettings.getPlayList(),
					playSettings.getVolume(),
					playSettings.isPlayOnJoin(),
					playSettings.getPlayMode(),
					playSettings.isShowingParticles(),
					playSettings.isReverseMode(),
					playSettings.isToggleMode(),
					playSettings.getRange(),
					playSettings.getCurrentSong()
			);

			if(playSettings.getFavorites().isEmpty()) return;

			for(GSong favoriteSong : playSettings.getFavorites()) {
				gMusicMain.getDataService().execute("INSERT INTO gmusic_play_settings_favorites (uuid, songId) VALUES (?, ?)", uuid.toString(), favoriteSong.getId());
			}
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not save play settings", e); }
	}

	public void removePlaySettingsCache(UUID uuid) { playSettingsCache.remove(uuid); }

	public void clearPlaySettingsCache() { playSettingsCache.clear(); }

}