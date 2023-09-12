package dev.geco.gmusic.objects;

import java.util.*;

public class PlaySettings {

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

	private List<Song> favorites;

	public PlaySettings(UUID UUID, int PlayList, long Volume, boolean PlayOnJoin, int PlayMode, boolean ShowParticles, boolean ReverseMode, boolean ToggleMode, long Range, String CurrentSong, List<Song> Favorites) {

		playlist = PlayList;
		uuid = UUID;
		volume = Volume;
		playOnJoin = PlayOnJoin;
		playMode = PlayMode;
		showParticles = ShowParticles;
		reverseMode = ReverseMode;
		toggleMode = ToggleMode;
		range = Range;
		currentSong = CurrentSong;
		favorites = Favorites;
	}

	public UUID getUUID() { return uuid; }

	public int getPlayList() { return playlist; }

	public void setPlayList(int PlayList) { playlist = PlayList; }

	public long getVolume() { return volume; }

	public float getFixedVolume() { return (float) (volume * 2) / 100; }

	public void setVolume(long Volume) { volume = Volume; }

	public boolean isPlayOnJoin() { return playOnJoin; }

	public void setPlayOnJoin(boolean PlayOnJoin) { playOnJoin = PlayOnJoin; }

	public int getPlayMode() { return playMode; }

	public void setPlayMode(int PlayMode) { playMode = PlayMode; }

	public boolean isShowingParticles() { return showParticles; }

	public void setShowParticles(boolean ShowingParticles) { showParticles = ShowingParticles; }

	public boolean isReverseMode() { return reverseMode; }

	public void setReverseMode(boolean ReverseMode) { reverseMode = ReverseMode; }

	public boolean isToggleMode() { return toggleMode; }

	public void setToggleMode(boolean ToggleMode) { toggleMode = ToggleMode; }

	public long getRange() { return range; }

	public void setRange(long Range) { range = Range; }

	public String getCurrentSong() { return currentSong; }

	public void setCurrentSong(String CurrentSong) { currentSong = CurrentSong; }

	public List<Song> getFavorites() { return favorites; }

	public void setFavorites(List<Song> Favorites) { favorites = Favorites; }

	public void addFavoriteSong(Song Song) { favorites.add(Song); }

	public void removeFavoriteSong(Song Song) { favorites.remove(Song); }

}