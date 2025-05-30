package dev.geco.gmusic.object;

import java.util.List;
import java.util.UUID;

public class GPlaySettings {

	private final UUID uuid;
	private GPlayListMode playlistMode;
	private int volume;
	private boolean playOnJoin;
	private GPlayMode playMode;
	private boolean showParticles;
	private boolean reverseMode;
	private boolean toggleMode;
	private long range;
	private String currentSong;
	private List<GSong> favorites;

	public GPlaySettings(
			UUID uuid,
			GPlayListMode playlistMode,
			int volume,
			boolean playOnJoin,
			GPlayMode playMode,
			boolean showParticles,
			boolean reverseMode,
			boolean toggleMode,
			long range,
			String currentSong,
			List<GSong> favorites
	) {
		this.uuid = uuid;
		this.playlistMode = playlistMode;
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

	public GPlayListMode getPlayListMode() { return playlistMode; }

	public void setPlayListMode(GPlayListMode playlistMode) { this.playlistMode = playlistMode; }

	public int getVolume() { return volume; }

	public float getFixedVolume() { return (float) (volume * 2) / 100; }

	public void setVolume(int volume) { this.volume = volume; }

	public boolean isPlayOnJoin() { return playOnJoin; }

	public void setPlayOnJoin(boolean playOnJoin) { this.playOnJoin = playOnJoin; }

	public GPlayMode getPlayMode() { return playMode; }

	public void setPlayMode(GPlayMode playMode) { this.playMode = playMode; }

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