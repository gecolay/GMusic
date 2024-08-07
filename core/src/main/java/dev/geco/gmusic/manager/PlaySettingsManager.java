package dev.geco.gmusic.manager;

import java.sql.*;
import java.util.*;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.objects.*;

public class PlaySettingsManager {

	private final GMusicMain GPM;

	public PlaySettingsManager(GMusicMain GPluginMain) { GPM = GPluginMain; }

	private final HashMap<UUID, PlaySettings> play_settings_cache = new HashMap<>();

	public void createTable() {
		try {
			GPM.getDManager().execute("CREATE TABLE IF NOT EXISTS play_settings (uuid TEXT, playList INTEGER, volume INTEGER, playOnJoin INTEGER, playMode INTEGER, showParticles INTEGER, reverseMode INTEGER, toggleMode INTEGER, range INTEGER, currentSong TEXT);");
			GPM.getDManager().execute("CREATE TABLE IF NOT EXISTS play_settings_favorites (uuid TEXT, songId TEXT);");
		} catch(Throwable e) { e.printStackTrace(); }
		play_settings_cache.clear();
	}

	public PlaySettings getPlaySettings(UUID UUID) {

		if(play_settings_cache.containsKey(UUID)) return play_settings_cache.get(UUID);

		List<Song> favorites = new ArrayList<>();

		PlaySettings playSettings = null;

		try {

			ResultSet playSettingsFavoritesData = GPM.getDManager().executeAndGet("SELECT * FROM play_settings_favorites WHERE uuid = ?", UUID.toString());

			while (playSettingsFavoritesData.next()) {

				favorites.add(GPM.getSongManager().getSongById(playSettingsFavoritesData.getString("songId")));
			}

			ResultSet playSettingsData = GPM.getDManager().executeAndGet("SELECT * FROM play_settings WHERE uuid = ?", UUID.toString());

			if(playSettingsData.next()) {

				playSettings = new PlaySettings(UUID, playSettingsData.getInt("playList"), playSettingsData.getLong("volume"), playSettingsData.getBoolean("playOnJoin"), playSettingsData.getInt("playMode"), playSettingsData.getBoolean("showParticles"), playSettingsData.getBoolean("reverseMode"), playSettingsData.getBoolean("toggleMode"), playSettingsData.getLong("range"), playSettingsData.getString("currentSong"), favorites);
			}

		} catch(Throwable e) { e.printStackTrace(); }

		if(playSettings == null) playSettings = generateDefaultPlaySettings(UUID);

		playSettings.setFavorites(favorites);

		play_settings_cache.put(UUID, playSettings);

		return playSettings;
	}

	private PlaySettings generateDefaultPlaySettings(UUID UUID) {

		return new PlaySettings(UUID, GPM.getCManager().PS_D_PLAYLIST, GPM.getCManager().PS_D_VOLUME, GPM.getCManager().R_PLAY_ON_JOIN, GPM.getCManager().PS_D_PLAY_MODE, GPM.getCManager().PS_D_PARTICLES, GPM.getCManager().PS_D_REVERSE, false, 0, null, new ArrayList<>());
	}

	public void setPlaySettings(UUID UUID, PlaySettings PlaySettings) {

		try {

			GPM.getDManager().execute("DELETE FROM play_settings WHERE uuid = ?", UUID.toString());
			GPM.getDManager().execute("DELETE FROM play_settings_favorites WHERE uuid = ?", UUID.toString());

			if(PlaySettings == null) {

				play_settings_cache.remove(UUID);
				return;
			}

			play_settings_cache.put(UUID, PlaySettings);

			GPM.getDManager().execute("INSERT INTO play_settings (uuid, playList, volume, playOnJoin, playMode, showParticles, reverseMode, toggleMode, range, currentSong) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					UUID.toString(),
					PlaySettings.getPlayList(),
					PlaySettings.getVolume(),
					PlaySettings.isPlayOnJoin(),
					PlaySettings.getPlayMode(),
					PlaySettings.isShowingParticles(),
					PlaySettings.isReverseMode(),
					PlaySettings.isToggleMode(),
					PlaySettings.getRange(),
					PlaySettings.getCurrentSong()
			);

			if(PlaySettings.getFavorites().isEmpty()) return;

			for(Song song : PlaySettings.getFavorites()) {

				GPM.getDManager().execute("INSERT INTO play_settings_favorites (uuid, songId) VALUES (?, ?)", UUID.toString(), song.getId());
			}
		} catch(Throwable e) { e.printStackTrace(); }
	}

	public void removePlaySettingsCache(UUID UUID) { play_settings_cache.remove(UUID); }

	public void clearPlaySettingsCache() { play_settings_cache.clear(); }

}