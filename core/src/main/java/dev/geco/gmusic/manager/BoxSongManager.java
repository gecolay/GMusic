package dev.geco.gmusic.manager;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.objects.*;

public class BoxSongManager {

	private final GMusicMain GPM;

	private final Random random = new Random();

	public BoxSongManager(GMusicMain GPluginMain) { GPM = GPluginMain; }

	public void playBoxSong(UUID UUID, Song Song) { playBoxSong(UUID, Song, 0); }

	private void playBoxSong(UUID UUID, Song Song, long Delay) {

		if(Song == null) return;

		SongSettings oldSongSettings = GPM.getPlaySongManager().getSongSettings(UUID);
		if(oldSongSettings != null) oldSongSettings.getTimer().cancel();

		Timer timer = new Timer();

		PlaySettings playSettings = GPM.getPlaySettingsManager().getPlaySettings(UUID);

		SongSettings songSettings = new SongSettings(Song, timer, playSettings.isReverseMode() ? Song.getLength() + Delay : -Delay);

		/*GPM.getTManager().run(() -> {
			Bukkit.getPluginManager().callEvent(new BoxSongPlayEvent(UUID, songSettings));
		});*/

		GPM.getPlaySongManager().putSongSettings(UUID, songSettings);

		playSettings.setCurrentSong(Song.getId());

		if(GPM.getCManager().A_SHOW_MESSAGES) {
			//TextComponent anpc = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-play", "%Title%", Song.getTitle(), "%Author%", Song.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : Song.getAuthor(), "%OAuthor%", Song.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : Song.getOriginalAuthor()));
			//for(Player P : GPM.getJukeBoxManager().getPlayersInRange(GPM.getValues().getJukeBlocks().get(UUID), playSettings.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}

		playBoxTimer(UUID, Song, timer);

	}

	private void playBoxTimer(UUID UUID, Song Song, Timer Timer) {

		SongSettings songSettings = GPM.getPlaySongManager().getSongSettings(UUID);

		Location L = null;//GPM.getJukeBoxManager().getJukeBlocks().get(UUID);
		if(L == null) return;

		//TextComponent anp = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-now-playing", "%Title%", Song.getTitle(), "%Author%", Song.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : Song.getAuthor(), "%OAuthor%", Song.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : Song.getOriginalAuthor()));

		GPM.getTManager().runAtFixedRate(() -> {

			long z = songSettings.getPosition();

			PlaySettings playSettings = GPM.getPlaySettingsManager().getPlaySettings(UUID);

			if(playSettings != null) {

				List<NotePart> lnp = Song.getContent().get(z);

				HashMap<Player, Double> pl = new HashMap<>();// GPM.getJukeBoxManager().getPlayersInRange(L, playSettings.getRange());

				if(lnp != null && playSettings.getVolume() > 0 && !pl.isEmpty()) {

					if(playSettings.isShowingParticles()) {
						Location PL = L.clone().add(random.nextDouble() - 0.5, 1, random.nextDouble() - 0.5);
						for(Player P : pl.keySet()) P.spawnParticle(Particle.NOTE, PL, 0, random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
					}

					for(NotePart np : lnp) {

						for(Player P : pl.keySet()) {

							if(np.getSound() != null) {

								float v = np.isVariableVolume() ? (float) ((pl.get(P) - playSettings.getRange()) * playSettings.getFixedVolume() / (double) -playSettings.getRange()) : np.getVolume();

								Location location = np.getDistance() == 0 ? P.getLocation() : GPM.getMusicUtil().getSteroNoteUtil().convertToStero(P.getLocation(), np.getDistance());

								if(!GPM.getCManager().ENVIRONMENT_EFFECTS) P.playSound(location, np.getSound(), Song.getSoundCategory(), v, np.getPitch());
								else {

									if(GPM.getMusicUtil().isPlayerSwimming(P)) P.playSound(location, np.getSound(), Song.getSoundCategory(), v > 0.4f ? v - 0.3f : v, np.getPitch() - 0.15f);
									else P.playSound(location, np.getSound(), Song.getSoundCategory(), v, np.getPitch());

								}

							} else if(np.getStopSound() != null) P.stopSound(np.getStopSound(), Song.getSoundCategory());

						}

					}

				}

				if(z == (playSettings.isReverseMode() ? 0 : Song.getLength())) {

					if(playSettings.getPlayMode() == 2 && playSettings.getPlayList() != 2) {

						z = playSettings.isReverseMode() ? Song.getLength() + GPM.getCManager().PS_TIME_UNTIL_REPEAT : -GPM.getCManager().PS_TIME_UNTIL_REPEAT;

						songSettings.setPosition(z);

					} else {

						Timer.cancel();

						if(playSettings.getPlayMode() == 1 && playSettings.getPlayList() != 2) playBoxSong(UUID, GPM.getPlaySongManager().getShuffleSong(UUID, Song), GPM.getCManager().PS_TIME_UNTIL_SHUFFLE);
						else {

							GPM.getPlaySongManager().removeSongSettings(UUID);

							MusicGUI m = GPM.getSongManager().getMusicGUIs().get(UUID);
							if(m != null) m.setPauseResumeBar();
						}
					}

				} else {

					songSettings.setPosition(playSettings.isReverseMode() ? z - 1 : z + 1);

					//if(GPM.getCManager().A_SHOW_WHILE_PLAYING) for(Player P : pl.keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anp);

				}

			} else Timer.cancel();
		}, 0, 1);

	}

	public Song getNextSong(UUID U) {

		SongSettings t = GPM.getPlaySongManager().getSongSettings(U);

		return t != null ? GPM.getPlaySongManager().getShuffleSong(U, t.getSong()) : GPM.getPlaySongManager().getRandomSong(U);
	}

	public void stopBoxSong(UUID U) {

		SongSettings t = GPM.getPlaySongManager().getSongSettings(U);

		if(t == null) return;

		/*BoxSongStopEvent bsse = new BoxSongStopEvent(U, t);

		Bukkit.getPluginManager().callEvent(bsse);*/

		t.getTimer().cancel();

		GPM.getPlaySongManager().removeSongSettings(U);

		PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(U);

		ps.setCurrentSong(null);

		if(GPM.getCManager().A_SHOW_MESSAGES && ps != null) {
			//TextComponent anpc = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-stop"));
			//for(Player P : GPM.getJukeBoxManager().getPlayersInRange(GPM.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}
	}

	public void pauseBoxSong(UUID U) {

		SongSettings t = GPM.getPlaySongManager().getSongSettings(U);

		if(t == null) return;

		/*BoxSongPauseEvent bspe = new BoxSongPauseEvent(U, t);

		Bukkit.getPluginManager().callEvent(bspe);*/

		t.getTimer().cancel();

		t.setPaused(true);

		PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(U);

		if(GPM.getCManager().A_SHOW_MESSAGES && ps != null) {
			//TextComponent anpc = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-pause"));
			//for(Player P : GPM.getJukeBoxManager().getPlayersInRange(GPM.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}
	}

	public void resumeBoxSong(UUID U) {

		SongSettings t = GPM.getPlaySongManager().getSongSettings(U);

		if(t == null) return;

		/*BoxSongResumeEvent bsre = new BoxSongResumeEvent(U, t);

		Bukkit.getPluginManager().callEvent(bsre);*/

		t.setTimer(new Timer());

		t.setPaused(false);

		PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(U);

		if(GPM.getCManager().A_SHOW_MESSAGES && ps != null) {
			//TextComponent anpc = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-resume"));
			//for(Player P : GPM.getJukeBoxManager().getPlayersInRange(GPM.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}

		playBoxTimer(U, t.getSong(), t.getTimer());
	}

}