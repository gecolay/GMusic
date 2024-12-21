package dev.geco.gmusic.manager;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.objects.*;

public class PlaySongManager {

	private final GMusicMain GPM;

	private final Random random = new Random();

	private final HashMap<UUID, SongSettings> song_settings = new HashMap<>();

	public PlaySongManager(GMusicMain GPluginMain) { GPM = GPluginMain; }

	public void playSong(Player Player, Song Song) { playSong(Player, Song, 0);}

	private void playSong(Player Player, Song Song, long Delay) {

		if(Song == null) return;

		PlaySettings playSettings = GPM.getPlaySettingsManager().getPlaySettings(Player.getUniqueId());

		if(playSettings.getPlayList() == 2) return;

		SongSettings oldSongSettings = getSongSettings(Player.getUniqueId());

		if(oldSongSettings != null) oldSongSettings.getTimer().cancel();

		Timer timer = new Timer();

		SongSettings songSettings = new SongSettings(Song, timer, playSettings.isReverseMode() ? Song.getLength() + Delay : -Delay);

		putSongSettings(Player.getUniqueId(), songSettings);

		playSettings.setCurrentSong(Song.getId());

		if(GPM.getCManager().A_SHOW_MESSAGES) GPM.getMManager().sendActionBarMessage(Player, "Messages.actionbar-play", "%Song%", Song.getId(), "%SongTitle%", Song.getTitle(), "%Author%", Song.getAuthor().isEmpty() ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : Song.getAuthor(), "%OAuthor%", Song.getOriginalAuthor().isEmpty() ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : Song.getOriginalAuthor());

		playTimer(Player, Song, timer);
	}

	public Song getRandomSong(UUID UUID) {
		PlaySettings playSettings = GPM.getPlaySettingsManager().getPlaySettings(UUID);
		List<Song> songs = playSettings.getPlayList() == 1 ? playSettings.getFavorites() : GPM.getSongManager().getSongs();
		return !songs.isEmpty() ? songs.get(random.nextInt(songs.size())) : null;
	}

	public Song getShuffleSong(UUID UUID, Song Song) {
		PlaySettings playSettings = GPM.getPlaySettingsManager().getPlaySettings(UUID);
		List<Song> s = playSettings.getPlayList() == 1 ? playSettings.getFavorites() : GPM.getSongManager().getSongs();
		return !s.isEmpty() ? s.indexOf(Song) + 1 == s.size() ? s.get(0) : s.get(s.indexOf(Song) + 1) : null;
	}

	private void playTimer(Player Player, Song Song, Timer Timer) {

		UUID u = Player.getUniqueId();

		SongSettings songSettings = getSongSettings(u);

		PlaySettings playSettings = GPM.getPlaySettingsManager().getPlaySettings(Player.getUniqueId());

		Timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {

				long position = songSettings.getPosition();

				List<NotePart> noteParts = Song.getContent().get(position);

				if(noteParts != null && playSettings.getVolume() > 0) {

					if(playSettings.isShowingParticles()) Player.spawnParticle(Particle.NOTE, Player.getEyeLocation().add(random.nextDouble() - 0.5, 0.3, random.nextDouble() - 0.5), 0, random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);

					for(NotePart np : noteParts) {

						if(np.getSound() != null) {

							float volume = playSettings.getFixedVolume() * np.getVolume();

							Location location = np.getDistance() == 0 ? Player.getLocation() : GPM.getMusicUtil().getSteroNoteUtil().convertToStero(Player.getLocation(), np.getDistance());

							if(!GPM.getCManager().ENVIRONMENT_EFFECTS) Player.playSound(location, np.getSound(), Song.getSoundCategory(), volume, np.getPitch());
							else {

								if(GPM.getMusicUtil().isPlayerSwimming(Player)) Player.playSound(location, np.getSound(), Song.getSoundCategory(), volume > 0.4f ? volume - 0.3f : volume, np.getPitch() - 0.15f);
								else Player.playSound(location, np.getSound(), Song.getSoundCategory(), volume, np.getPitch());
							}
						} else if(np.getStopSound() != null) Player.stopSound(np.getStopSound(), Song.getSoundCategory());
					}
				}

				if(position == (playSettings.isReverseMode() ? 0 : Song.getLength())) {

					if(playSettings.getPlayMode() == 2) {

						position = playSettings.isReverseMode() ? Song.getLength() + GPM.getCManager().PS_TIME_UNTIL_REPEAT : -GPM.getCManager().PS_TIME_UNTIL_REPEAT;

						songSettings.setPosition(position);
					} else {

						Timer.cancel();

						if(playSettings.getPlayMode() == 1) playSong(Player, getShuffleSong(u, Song), GPM.getCManager().PS_TIME_UNTIL_SHUFFLE);
						else {

							song_settings.remove(u);

							MusicGUI m = GPM.getSongManager().getMusicGUIs().get(u);

							if(m != null) m.setPauseResumeBar();
						}
					}
					return;
				}

				songSettings.setPosition(playSettings.isReverseMode() ? position - 1 : position + 1);

				if(GPM.getCManager().A_SHOW_WHILE_PLAYING) GPM.getMManager().sendActionBarMessage(Player, "Messages.actionbar-playing", "%Song%", Song.getId(), "%SongTitle%", Song.getTitle(), "%Author%", Song.getAuthor().isEmpty() ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : Song.getAuthor(), "%OAuthor%", Song.getOriginalAuthor().isEmpty() ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : Song.getOriginalAuthor());
			}
		}, 0, 1);
	}

	public SongSettings getSongSettings(UUID UUID) { return song_settings.get(UUID); }

	public void removeSongSettings(UUID UUID) { song_settings.remove(UUID); }

	public void putSongSettings(UUID UUID, SongSettings SongSettings) { song_settings.put(UUID, SongSettings); }

	public boolean hasPlayingSong(UUID UUID) { return getSongSettings(UUID) != null; }

	public boolean hasPausedSong(UUID UUID) { return getSongSettings(UUID) != null && getSongSettings(UUID).isPaused(); }

	public Song getPlayingSong(UUID UUID) { return getSongSettings(UUID).getSong(); }

	public Song getNextSong(Player Player) {

		SongSettings songSettings = getSongSettings(Player.getUniqueId());

		return songSettings != null ? getShuffleSong(Player.getUniqueId(), songSettings.getSong()) : getRandomSong(Player.getUniqueId());
	}

	public void stopSong(Player Player) {

		SongSettings songSettings = getSongSettings(Player.getUniqueId());

		if(songSettings == null) return;

		songSettings.getTimer().cancel();

		song_settings.remove(Player.getUniqueId());

		PlaySettings playSettings = GPM.getPlaySettingsManager().getPlaySettings(Player.getUniqueId());

		playSettings.setCurrentSong(null);

		if(GPM.getCManager().A_SHOW_MESSAGES) GPM.getMManager().sendActionBarMessage(Player, "Messages.actionbar-stop");
	}

	public void pauseSong(Player Player) {

		SongSettings songSettings = getSongSettings(Player.getUniqueId());

		if(songSettings == null) return;

		songSettings.getTimer().cancel();

		songSettings.setPaused(true);

		if(GPM.getCManager().A_SHOW_MESSAGES) GPM.getMManager().sendActionBarMessage(Player, "Messages.actionbar-pause");
	}

	public void resumeSong(Player Player) {

		SongSettings songSettings = getSongSettings(Player.getUniqueId());

		if(songSettings == null) return;

		songSettings.setTimer(new Timer());

		songSettings.setPaused(false);

		if(GPM.getCManager().A_SHOW_MESSAGES) GPM.getMManager().sendActionBarMessage(Player, "Messages.actionbar-resume");

		playTimer(Player, songSettings.getSong(), songSettings.getTimer());
	}

}