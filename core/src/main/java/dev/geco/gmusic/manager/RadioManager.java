package dev.geco.gmusic.manager;

import java.util.*;

import dev.geco.gmusic.util.MusicUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.*;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.objects.*;

public class RadioManager {

	private final GMusicMain GPM;
	private final Random random = new Random();
	private final UUID radioUUID = UUID.randomUUID();
	private final List<Player> radioPlayers = new ArrayList<>();

	public RadioManager(GMusicMain GPluginMain) { GPM = GPluginMain; }

	public void playRadio() {

		PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(radioUUID);
		GPM.getPlaySettingsManager().setPlaySettings(radioUUID, ps);

		playRadioSong(GPM.getPlaySongManager().getRandomSong(radioUUID), 0);
	}

	public void playRadioSong(Song Song, long Delay) {

		if(Song == null) return;

		SongSettings oldSongSettings = GPM.getPlaySongManager().getSongSettings(radioUUID);
		if(oldSongSettings != null) oldSongSettings.getTimer().cancel();

		PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(radioUUID);

		Timer timer = new Timer();

		SongSettings songSettings = new SongSettings(Song, timer, ps.isReverseMode() ? Song.getLength() + Delay : -Delay);

		GPM.getPlaySongManager().putSongSettings(radioUUID, songSettings);

		ps.setCurrentSong(Song.getId());

		if(GPM.getCManager().A_SHOW_MESSAGES) {
			for(Player radioPlayer : radioPlayers) GPM.getMManager().sendActionBarMessage(radioPlayer, "Messages.actionbar-play", "%Title%", Song.getTitle(), "%Author%", Song.getAuthor().isEmpty() ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : Song.getAuthor(), "%OAuthor%", Song.getOriginalAuthor().isEmpty() ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : Song.getOriginalAuthor());
		}

		//for(UUID uuid : GPM.getRadioManager().getRadioJukeBoxes()) GPM.getBoxSongManager().playBoxSong(uuid, Song);

		playTimer(Song, timer);

	}

	private void playTimer(Song S, Timer T) {

		SongSettings songSettings = GPM.getPlaySongManager().getSongSettings(radioUUID);

		//TextComponent anp = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-now-playing", "%Title%", S.getTitle(), "%Author%", S.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : S.getAuthor(), "%OAuthor%", S.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : S.getOriginalAuthor()));

		GPM.getTManager().runAtFixedRate(() -> {

			long z = songSettings.getPosition();

			PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(radioUUID);

			if(ps != null) {

				List<NotePart> lnp = S.getContent().get(z);

                List<Player> pl = new ArrayList<>(radioPlayers);

				if(lnp != null && ps.getVolume() > 0 && !pl.isEmpty()) {

					for(Player P : pl) {
						PlaySettings ps1 = GPM.getPlaySettingsManager().getPlaySettings(P.getUniqueId());
						if(ps1.isShowingParticles()) P.spawnParticle(Particle.NOTE, P.getEyeLocation().clone().add(random.nextDouble() - 0.5, 0.3, random.nextDouble() - 0.5), 0, random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
					}

					for(NotePart np : lnp) {

						for(Player P : pl) {

							if(np.getSound() != null) {

								PlaySettings ps1 = GPM.getPlaySettingsManager().getPlaySettings(P.getUniqueId());

								float v = ps1.getFixedVolume() * np.getVolume();

								Location L = np.getDistance() == 0 ? P.getLocation() : GPM.getMusicUtil().getSteroNoteUtil().convertToStero(P.getLocation(), np.getDistance());

								if(!GPM.getCManager().ENVIRONMENT_EFFECTS) P.playSound(L, np.getSound(), S.getSoundCategory(), v, np.getPitch());
								else {

									if(GPM.getMusicUtil().isPlayerSwimming(P)) P.playSound(L, np.getSound(), S.getSoundCategory(), v > 0.4f ? v - 0.3f : v, np.getPitch() - 0.15f);
									else P.playSound(L, np.getSound(), S.getSoundCategory(), v, np.getPitch());

								}

							} else if(np.getStopSound() != null) P.stopSound(np.getStopSound(), S.getSoundCategory());

						}

					}

				}

				if(z == (ps.isReverseMode() ? 0 : S.getLength())) {

					T.cancel();

					playRadioSong(GPM.getPlaySongManager().getShuffleSong(radioUUID, S), GPM.getCManager().PS_TIME_UNTIL_SHUFFLE);

				} else {

					songSettings.setPosition(ps.isReverseMode() ? z - 1 : z + 1);

					//if(GPM.getCManager().A_SHOW_ALWAYS_WHILE_PLAYING) for(Player P : pl) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anp);

				}

			} else T.cancel();
		}, 0, 1);

	}

	public void stopRadio() {

		SongSettings songSettings = GPM.getPlaySongManager().getSongSettings(radioUUID);

		if(songSettings == null) return;

		songSettings.getTimer().cancel();

		GPM.getPlaySongManager().removeSongSettings(radioUUID);
		GPM.getPlaySettingsManager().setPlaySettings(radioUUID, null);
	}

	public void removeRadioPlayer(Player Player) { radioPlayers.remove(Player); }

	public void addRadioPlayer(Player Player) { radioPlayers.add(Player); }

}