package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.object.GNotePart;
import dev.geco.gmusic.object.GPlaySettings;
import dev.geco.gmusic.object.GSong;
import dev.geco.gmusic.object.GPlayState;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

public class RadioService {

	private final GMusicMain gMusicMain;
	private final Random random = new Random();
	private final UUID radioUUID = UUID.randomUUID();
	private final Set<Player> radioPlayers = new HashSet<>();

	public RadioService(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	public void startRadio() {
		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(radioUUID);
		gMusicMain.getPlaySettingsService().setPlaySettings(radioUUID, playSettings);

		playRadioSong(gMusicMain.getPlaySongService().getRandomSong(radioUUID), 0);
	}

	public void playRadioSong(GSong song, long delay) {
		if(song == null) return;

		GPlayState oldSongSettings = gMusicMain.getPlaySongService().getPlayState(radioUUID);
		if(oldSongSettings != null) oldSongSettings.getTimer().cancel();

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(radioUUID);

		Timer timer = new Timer();

		GPlayState songSettings = new GPlayState(song, timer, playSettings.isReverseMode() ? song.getLength() + delay : -delay);

		gMusicMain.getPlaySongService().putPlayState(radioUUID, songSettings);

		playSettings.setCurrentSong(song.getId());

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) {
			for(Player radioPlayer : radioPlayers) gMusicMain.getMessageService().sendActionBarMessage(radioPlayer, "Messages.actionbar-play", "%Title%", song.getTitle(), "%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(), "%OAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-oauthor") : song.getOriginalAuthor());
		}

		for(UUID uuid : gMusicMain.getBoxSongService().getRadioJukeBoxes()) gMusicMain.getBoxSongService().playBoxSong(uuid, GSong);

		playTimer(song, timer);
	}

	private void playTimer(GSong song, Timer timer) {
		GPlayState songSettings = gMusicMain.getPlaySongService().getPlayState(radioUUID);

		//TextComponent anp = new TextComponent(gMusicMain.getMManager().getMessage("Messages.actionbar-now-playing", "%Title%", S.getTitle(), "%Author%", S.getAuthor().equals("") ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-author") : S.getAuthor(), "%OAuthor%", S.getOriginalAuthor().equals("") ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : S.getOriginalAuthor()));

		gMusicMain.getTaskService().runAtFixedRate(() -> {
			long position = songSettings.getTickPosition();

			GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(radioUUID);

			if(playSettings != null) {
				List<GNotePart> noteParts = song.getContent().get(position);

                List<Player> players = new ArrayList<>(radioPlayers);

				if(noteParts != null && playSettings.getVolume() > 0 && !players.isEmpty()) {

					for(Player player : players) {
						GPlaySettings ps1 = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId());
						if(ps1.isShowingParticles()) player.spawnParticle(Particle.NOTE, player.getEyeLocation().clone().add(random.nextDouble() - 0.5, 0.3, random.nextDouble() - 0.5), 0, random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
					}

					for(GNotePart np : noteParts) {
						for(Player player : players) {
							if(np.getSound() != null) {
								GPlaySettings ps1 = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId());

								float volume = ps1.getFixedVolume() * np.getVolume();

								Location location = np.getDistance() == 0 ? player.getLocation() : gMusicMain.getSteroNoteUtil().convertToStero(player.getLocation(), np.getDistance());

								if(!gMusicMain.getConfigService().ENVIRONMENT_EFFECTS) player.playSound(location, np.getSound(), song.getSoundCategory(), volume, np.getPitch());
								else {
									if(gMusicMain.getMusicUtil().isPlayerSwimming(player)) player.playSound(location, np.getSound(), song.getSoundCategory(), volume > 0.4f ? volume - 0.3f : volume, np.getPitch() - 0.15f);
									else player.playSound(location, np.getSound(), song.getSoundCategory(), volume, np.getPitch());
								}
							} else if(np.getStopSound() != null) player.stopSound(np.getStopSound(), song.getSoundCategory());
						}
					}
				}

				if(position == (playSettings.isReverseMode() ? 0 : song.getLength())) {
					timer.cancel();
					playRadioSong(gMusicMain.getPlaySongService().getShuffleSong(radioUUID, song), gMusicMain.getConfigService().PS_TIME_UNTIL_SHUFFLE);

				} else {
					songSettings.setTickPosition(playSettings.isReverseMode() ? position - 1 : position + 1);
					//if(gMusicMain.getCManager().A_SHOW_ALWAYS_WHILE_PLAYING) for(Player P : pl) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anp);
				}
			} else timer.cancel();
		}, 0, 1);
	}

	public void stopRadio() {
		GPlayState songSettings = gMusicMain.getPlaySongService().getPlayState(radioUUID);
		if(songSettings == null) return;

		songSettings.getTimer().cancel();

		gMusicMain.getPlaySongService().removePlayState(radioUUID);
		gMusicMain.getPlaySettingsService().setPlaySettings(radioUUID, null);
	}

	public void removeRadioPlayer(Player Player) { radioPlayers.remove(Player); }

	public void addRadioPlayer(Player Player) { radioPlayers.add(Player); }

}