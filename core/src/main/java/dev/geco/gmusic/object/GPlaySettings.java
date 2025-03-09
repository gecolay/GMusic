package dev.geco.gmusic.object;

import java.util.List;
import java.util.UUID;

public class GPlaySettings {

	private final UUID uuid;
	private int playlist;
	private long volume;
	private boolean playOnJoin;
	private int playMode;
	private boolean showParticles;
	private boolean reverseMode;
	private boolean toggleMode;
	private long range;
	private String currentSong;
	private List<GSong> favorites;

	public GPlaySettings(UUID uuid, int playlist, long volume, boolean playOnJoin, int playMode, boolean showParticles, boolean reverseMode, boolean toggleMode, long range, String currentSong, List<GSong> favorites) {
		this.uuid = uuid;
		this.playlist = playlist;
		this.volume = volume;
		this.playOnJoin = playOnJoin;
		this.playMode = playMode;
		this.showParticles = showParticles;
		this.reverseMode = reverseMode;
		this.toggleMode = toggleMode;
		this.range = range;
		this.currentSong = currentSong;
		this.favorites = favorites;
	}

	public UUID getUUID() { return uuid; }

	public int getPlayList() { return playlist; }

	public void setPlayList(int playlist) { this.playlist = playlist; }

	public long getVolume() { return volume; }

	public float getFixedVolume() { return (float) (volume * 2) / 100; }

	public void setVolume(long volume) { this.volume = volume; }

	public boolean isPlayOnJoin() { return playOnJoin; }

	public void setPlayOnJoin(boolean playOnJoin) { this.playOnJoin = playOnJoin; }

	public int getPlayMode() { return playMode; }

	public void setPlayMode(int playMode) { this.playMode = playMode; }

	public boolean isShowingParticles() { return showParticles; }

	public void setShowParticles(boolean showParticles) { this.showParticles = showParticles; }

	public boolean isReverseMode() { return reverseMode; }

	public void setReverseMode(boolean reverseMode) { this.reverseMode = reverseMode; }

	public boolean isToggleMode() { return toggleMode; }

	public void setToggleMode(boolean toggleMode) { this.toggleMode = toggleMode; }

	public long getRange() { return range; }

	public void setRange(long range) { this.range = range; }

	public String getCurrentSong() { return currentSong; }

	public void setCurrentSong(String currentSong) { this.currentSong = currentSong; }

	public List<GSong> getFavorites() { return favorites; }

	public void setFavorites(List<GSong> favorites) { this.favorites = favorites; }

	public void addFavoriteSong(GSong song) { favorites.add(song); }

	public void removeFavoriteSong(GSong song) { favorites.remove(song); }

}