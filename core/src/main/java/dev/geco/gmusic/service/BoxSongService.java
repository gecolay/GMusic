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
import java.util.UUID;

public class BoxSongService {

	private final GMusicMain gMusicMain;
	private final Random random = new Random();

	public BoxSongService(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	public void playBoxSong(UUID uuid, GSong song) { playBoxSong(uuid, song, 0); }

	private void playBoxSong(UUID uuid, GSong song, long delay) {
		if(song == null) return;

		GPlayState playState = gMusicMain.getPlaySongService().getPlayState(uuid);
		if(playState != null) playState.getTimer().cancel();

		Timer timer = new Timer();

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);

		playState = new GPlayState(song, timer, playSettings.isReverseMode() ? song.getLength() + delay : -delay);

		/*gMusicMain.getTManager().run(() -> {
			Bukkit.getPluginManager().callEvent(new BoxSongPlayEvent(UUID, playState));
		});*/

		gMusicMain.getPlaySongService().putPlayState(uuid, playState);

		playSettings.setCurrentSong(song.getId());

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) {
			//TextComponent anpc = new TextComponent(gMusicMain.getMManager().getMessage("Messages.actionbar-play", "%Title%", Song.getTitle(), "%Author%", Song.getAuthor().equals("") ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-author") : Song.getAuthor(), "%OAuthor%", Song.getOriginalAuthor().equals("") ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : Song.getOriginalAuthor()));
			//for(Player P : gMusicMain.getJukeBoxManager().getPlayersInRange(gMusicMain.getValues().getJukeBlocks().get(UUID), playSettings.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}

		playBoxTimer(uuid, song, timer);
	}

	private void playBoxTimer(UUID uuid, GSong song, Timer timer) {
		GPlayState playState = gMusicMain.getPlaySongService().getPlayState(uuid);

		Location L = null;//gMusicMain.getJukeBoxManager().getJukeBlocks().get(UUID);
		if(L == null) return;

		//TextComponent anp = new TextComponent(gMusicMain.getMManager().getMessage("Messages.actionbar-now-playing", "%Title%", Song.getTitle(), "%Author%", Song.getAuthor().equals("") ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-author") : Song.getAuthor(), "%OAuthor%", Song.getOriginalAuthor().equals("") ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : Song.getOriginalAuthor()));

		gMusicMain.getTManager().runAtFixedRate(() -> {
			long z = playState.getTickPosition();

			GPlaySettings playSettings = gMusicMain.getPlaySettingsManager().getPlaySettings(uuid);

			if(playSettings != null) {
				List<GNotePart> lnp = song.getContent().get(z);

				HashMap<Player, Double> pl = new HashMap<>();// gMusicMain.getJukeBoxManager().getPlayersInRange(L, playSettings.getRange());

				if(lnp != null && playSettings.getVolume() > 0 && !pl.isEmpty()) {
					if(playSettings.isShowingParticles()) {
						Location PL = L.clone().add(random.nextDouble() - 0.5, 1, random.nextDouble() - 0.5);
						for(Player P : pl.keySet()) P.spawnParticle(Particle.NOTE, PL, 0, random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
					}

					for(GNotePart np : lnp) {
						for(Player P : pl.keySet()) {
							if(np.getSound() != null) {
								float volume = (float) ((pl.get(P) - playSettings.getRange()) * playSettings.getFixedVolume() / (double) -playSettings.getRange()) * np.getVolume();

								Location location = np.getDistance() == 0 ? P.getLocation() : gMusicMain.getMusicUtil().getSteroNoteUtil().convertToStero(P.getLocation(), np.getDistance());

								if(!gMusicMain.getCManager().ENVIRONMENT_EFFECTS) P.playSound(location, np.getSound(), song.getSoundCategory(), volume, np.getPitch());
								else {
									if(gMusicMain.getMusicUtil().isPlayerSwimming(P)) P.playSound(location, np.getSound(), song.getSoundCategory(), volume > 0.4f ? volume - 0.3f : volume, np.getPitch() - 0.15f);
									else P.playSound(location, np.getSound(), song.getSoundCategory(), volume, np.getPitch());
								}
							} else if(np.getStopSound() != null) P.stopSound(np.getStopSound(), song.getSoundCategory());
						}
					}
				}

				if(z == (playSettings.isReverseMode() ? 0 : song.getLength())) {
					if(playSettings.getPlayMode() == 2 && playSettings.getPlayList() != 2) {
						z = playSettings.isReverseMode() ? song.getLength() + gMusicMain.getCManager().PS_TIME_UNTIL_REPEAT : -gMusicMain.getCManager().PS_TIME_UNTIL_REPEAT;
						playState.setTickPosition(z);
					} else {
						timer.cancel();

						if(playSettings.getPlayMode() == 1 && playSettings.getPlayList() != 2) playBoxSong(uuid, gMusicMain.getPlaySongManager().getShuffleSong(uuid, song), gMusicMain.getCManager().PS_TIME_UNTIL_SHUFFLE);
						else {
							gMusicMain.getPlaySongManager().removeSongSettings(uuid);
							GMusicGUI m = gMusicMain.getSongManager().getMusicGUIs().get(uuid);
							if(m != null) m.setPauseResumeBar();
						}
					}
				} else {
					playState.setTickPosition(playSettings.isReverseMode() ? z - 1 : z + 1);
					//if(gMusicMain.getCManager().A_SHOW_WHILE_PLAYING) for(Player P : pl.keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anp);
				}
			} else timer.cancel();
		}, 0, 1);
	}

	public GSong getNextSong(UUID uuid) {
		GPlayState playState = gMusicMain.getPlaySongService().getPlayState(uuid);
		return playState != null ? gMusicMain.getPlaySongService().getShuffleSong(uuid, playState.getSong()) : gMusicMain.getPlaySongService().getRandomSong(uuid);
	}

	public void stopBoxSong(UUID uuid) {
		GPlayState playState = gMusicMain.getPlaySongService().getPlayState(uuid);
		if(playState == null) return;

		/*BoxSongStopEvent bsse = new BoxSongStopEvent(U, t);
		Bukkit.getPluginManager().callEvent(bsse);*/

		playState.getTimer().cancel();

		gMusicMain.getPlaySongService().removePlayState(uuid);

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);
		playSettings.setCurrentSong(null);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES && playSettings != null) {
			//TextComponent anpc = new TextComponent(gMusicMain.getMManager().getMessage("Messages.actionbar-stop"));
			//for(Player P : gMusicMain.getJukeBoxManager().getPlayersInRange(gMusicMain.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}
	}

	public void pauseBoxSong(UUID uuid) {
		GPlayState playState = gMusicMain.getPlaySongService().getPlayState(uuid);
		if(playState == null) return;

		/*BoxSongPauseEvent bspe = new BoxSongPauseEvent(U, t);
		Bukkit.getPluginManager().callEvent(bspe);*/

		playState.getTimer().cancel();
		playState.setPaused(true);

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);
		if(gMusicMain.getConfigService().A_SHOW_MESSAGES && playSettings != null) {
			//TextComponent anpc = new TextComponent(gMusicMain.getMManager().getMessage("Messages.actionbar-pause"));
			//for(Player P : gMusicMain.getJukeBoxManager().getPlayersInRange(gMusicMain.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}
	}

	public void resumeBoxSong(UUID uuid) {
		GPlayState playState = gMusicMain.getPlaySongService().getPlayState(uuid);
		if(playState == null) return;

		/*BoxSongResumeEvent bsre = new BoxSongResumeEvent(U, t);
		Bukkit.getPluginManager().callEvent(bsre);*/

		playState.setTimer(new Timer());
		playState.setPaused(false);

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);
		if(gMusicMain.getConfigService().A_SHOW_MESSAGES && playSettings != null) {
			//TextComponent anpc = new TextComponent(gMusicMain.getMManager().getMessage("Messages.actionbar-resume"));
			//for(Player P : gMusicMain.getJukeBoxManager().getPlayersInRange(gMusicMain.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}

		playBoxTimer(uuid, playState.getSong(), playState.getTimer());
	}

}