package dev.geco.gmusic.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlaySettings {

	private final @NotNull UUID uuid;
	private final @NotNull PlayType playType;
	private @NotNull PlayListMode playlistMode;
	private int volume;
	private @NotNull PlayMode playMode;
	private boolean showParticles;
	private boolean reverseMode;
	private boolean toggleMode;
	private long range;
	private boolean stereo;
	private @NotNull List<Song> favorites;

	public PlaySettings(
			@NotNull UUID uuid,
			@NotNull PlayType playType,
			@NotNull PlayListMode playlistMode,
			int volume,
			@NotNull PlayMode playMode,
			boolean showParticles,
			boolean reverseMode,
			boolean toggleMode,
			long range,
			boolean stereo,
			@NotNull List<Song> favorites
	) {
		this.uuid = uuid;
		this.playType = playType;
		this.playlistMode = playlistMode;
		this.volume = volume;
		this.playMode = playMode;
		this.showParticles = showParticles;
		this.reverseMode = reverseMode;
		this.toggleMode = toggleMode;
		this.range = range;
		this.stereo = stereo;
		this.favorites = favorites;
	}

	public @NotNull UUID getUUID() { return uuid; }

	public @NotNull PlayType getPlayType() { return playType; }

	public @NotNull PlayListMode getPlayListMode() { return playlistMode; }

	public void setPlayListMode(@NotNull PlayListMode playlistMode) { this.playlistMode = playlistMode; }

	public int getVolume() { return volume; }

	public float getFixedVolume() { return (float) (volume * 2) / 100; }

	public void setVolume(int volume) { this.volume = volume; }

	public @NotNull PlayMode getPlayMode() { return playMode; }

	public void setPlayMode(@NotNull PlayMode playMode) { this.playMode = playMode; }

	public boolean isShowingParticles() { return showParticles; }

	public void setShowParticles(boolean showParticles) { this.showParticles = showParticles; }

	public boolean isReverseMode() { return reverseMode; }

	public void setReverseMode(boolean reverseMode) { this.reverseMode = reverseMode; }

	public boolean isToggleMode() { return toggleMode; }

	public void setToggleMode(boolean toggleMode) { this.toggleMode = toggleMode; }

	public long getRange() { return range; }

	public void setRange(long range) { this.range = range; }

	public boolean isStereo() { return stereo; }

	public void setStereo(boolean stereo) { this.stereo = stereo; }

	public @NotNull List<Song> getFavorites() { return favorites; }

	public void setFavorites(@NotNull List<Song> favorites) { this.favorites = favorites; }

	public void addFavoriteSong(@NotNull Song song) { favorites.add(song); }

	public void removeFavoriteSong(@NotNull Song song) { favorites.remove(song); }

}