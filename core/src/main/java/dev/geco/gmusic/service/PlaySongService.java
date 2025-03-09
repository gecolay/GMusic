package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.object.GMusicGUI;
import dev.geco.gmusic.object.GNotePart;
import dev.geco.gmusic.object.GPlaySettings;
import dev.geco.gmusic.object.GSong;
import dev.geco.gmusic.object.GPlayState;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PlaySongService {

	private final GMusicMain gMusicMain;
	private final Random random = new Random();
	private final HashMap<UUID, GPlayState> songPlayStates = new HashMap<>();

	public PlaySongService(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	public void playSong(Player player, GSong song) { playSong(player, song, 0);}

	private void playSong(Player player, GSong song, long delay) {
		if(song == null) return;

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId());
		if(playSettings.getPlayList() == 2) return;

		GPlayState oldSongSettings = getPlayState(player.getUniqueId());
		if(oldSongSettings != null) oldSongSettings.getTimer().cancel();

		Timer timer = new Timer();
		GPlayState songSettings = new GPlayState(song, timer, playSettings.isReverseMode() ? song.getLength() + delay : -delay);
		putPlayState(player.getUniqueId(), songSettings);

		playSettings.setCurrentSong(song.getId());

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-play", "%Song%", song.getId(), "%SongTitle%", song.getTitle(), "%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(), "%OAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : song.getOriginalAuthor());

		startSong(player, song, timer);
	}

	public GSong getRandomSong(UUID uuid) {
		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);
		List<GSong> songs = playSettings.getPlayList() == 1 ? playSettings.getFavorites() : gMusicMain.getSongService().getSongs();
		return !songs.isEmpty() ? songs.get(random.nextInt(songs.size())) : null;
	}

	public GSong getShuffleSong(UUID uuid, GSong song) {
		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);
		List<GSong> songs = playSettings.getPlayList() == 1 ? playSettings.getFavorites() : gMusicMain.getSongService().getSongs();
		return !songs.isEmpty() ? songs.indexOf(song) + 1 == songs.size() ? songs.get(0) : songs.get(songs.indexOf(song) + 1) : null;
	}

	private void startSong(Player player, GSong song, Timer timer) {
		UUID uuid = player.getUniqueId();

		GPlayState songSettings = getPlayState(uuid);

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId());

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				long position = songSettings.getTickPosition();

				List<GNotePart> noteParts = song.getContent().get(position);

				if(noteParts != null && playSettings.getVolume() > 0) {
					if(playSettings.isShowingParticles()) player.spawnParticle(Particle.NOTE, player.getEyeLocation().add(random.nextDouble() - 0.5, 0.3, random.nextDouble() - 0.5), 0, random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);

					for(GNotePart notePart : noteParts) {
						if(notePart.getSound() != null) {
							float volume = playSettings.getFixedVolume() * notePart.getVolume();

							Location location = notePart.getDistance() == 0 ? player.getLocation() : gMusicMain.getSteroNoteUtil().convertToStero(player.getLocation(), notePart.getDistance());

							if(!gMusicMain.getConfigService().ENVIRONMENT_EFFECTS) player.playSound(location, notePart.getSound(), song.getSoundCategory(), volume, notePart.getPitch());
							else {
								if(gMusicMain.getMusicUtil().isPlayerSwimming(player)) player.playSound(location, notePart.getSound(), song.getSoundCategory(), volume > 0.4f ? volume - 0.3f : volume, notePart.getPitch() - 0.15f);
								else player.playSound(location, notePart.getSound(), song.getSoundCategory(), volume, notePart.getPitch());
							}
						} else if(notePart.getStopSound() != null) player.stopSound(notePart.getStopSound(), song.getSoundCategory());
					}
				}

				if(position == (playSettings.isReverseMode() ? 0 : song.getLength())) {
					if(playSettings.getPlayMode() == 2) {
						position = playSettings.isReverseMode() ? song.getLength() + gMusicMain.getConfigService().PS_TIME_UNTIL_REPEAT : -gMusicMain.getConfigService().PS_TIME_UNTIL_REPEAT;
						songSettings.setTickPosition(position);
					} else {
						timer.cancel();

						if(playSettings.getPlayMode() == 1) playSong(player, getShuffleSong(uuid, song), gMusicMain.getConfigService().PS_TIME_UNTIL_SHUFFLE);
						else {
							PlaySongService.this.songPlayStates.remove(uuid);
							GMusicGUI m = gMusicMain.getSongService().getMusicGUIs().get(uuid);
							if(m != null) m.setPauseResumeBar();
						}
					}
					return;
				}

				songSettings.setTickPosition(playSettings.isReverseMode() ? position - 1 : position + 1);

				if(gMusicMain.getConfigService().A_SHOW_WHILE_PLAYING) gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-playing", "%Song%", song.getId(), "%SongTitle%", song.getTitle(), "%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(), "%OAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : song.getOriginalAuthor());
			}
		}, 0, 1);
	}

	public GPlayState getPlayState(UUID uuid) { return songPlayStates.get(uuid); }

	public void removePlayState(UUID uuid) { songPlayStates.remove(uuid); }

	public void putPlayState(UUID uuid, GPlayState playState) { songPlayStates.put(uuid, playState); }

	public boolean hasPlayingSong(UUID uuid) { return getPlayState(uuid) != null; }

	public boolean hasPausedSong(UUID uuid) { return getPlayState(uuid) != null && getPlayState(uuid).isPaused(); }

	public GSong getPlayingSong(UUID uuid) { return getPlayState(uuid).getSong(); }

	public GSong getNextSong(Player player) {
		GPlayState songSettings = getPlayState(player.getUniqueId());
		return songSettings != null ? getShuffleSong(player.getUniqueId(), songSettings.getSong()) : getRandomSong(player.getUniqueId());
	}

	public void stopSong(Player player) {
		GPlayState songSettings = getPlayState(player.getUniqueId());
		if(songSettings == null) return;

		songSettings.getTimer().cancel();

		this.songPlayStates.remove(player.getUniqueId());

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId());
		playSettings.setCurrentSong(null);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-stop");
	}

	public void pauseSong(Player player) {
		GPlayState songSettings = getPlayState(player.getUniqueId());
		if(songSettings == null) return;

		songSettings.getTimer().cancel();
		songSettings.setPaused(true);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-pause");
	}

	public void resumeSong(Player player) {
		GPlayState songSettings = getPlayState(player.getUniqueId());
		if(songSettings == null) return;

		songSettings.setTimer(new Timer());
		songSettings.setPaused(false);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-resume");

		startSong(player, songSettings.getSong(), songSettings.getTimer());
	}

}