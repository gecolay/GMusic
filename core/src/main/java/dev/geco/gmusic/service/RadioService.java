package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.model.NotePart;
import dev.geco.gmusic.model.PlayListMode;
import dev.geco.gmusic.model.PlayMode;
import dev.geco.gmusic.model.PlaySettings;
import dev.geco.gmusic.model.PlayState;
import dev.geco.gmusic.model.PlayType;
import dev.geco.gmusic.model.Song;
import dev.geco.gmusic.model.gui.MusicGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class RadioService {

	public static final UUID RADIO_UUID = UUID.nameUUIDFromBytes(GMusicMain.NAME.getBytes(StandardCharsets.UTF_8));

	private final GMusicMain gMusicMain;
	private final Random random = new Random();
	private final Set<Player> radioPlayers = new HashSet<>();
	private final HashMap<UUID, Block> radioJukeBoxBlocks = new HashMap<>();

	public RadioService(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	public void startRadio() {
		radioPlayers.clear();
		radioJukeBoxBlocks.clear();

		for(Player player : Bukkit.getOnlinePlayers()) {
			PlaySettings playerPlaySettings = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId(), PlayType.DEFAULT);
			if(playerPlaySettings.getPlayListMode() == PlayListMode.RADIO) {
				radioPlayers.add(player);
			}
		}

		PlayState playState = gMusicMain.getPlayService().getPlayState(RADIO_UUID);
		Song song = playState != null ? playState.getSong() : null;
		long delay = playState != null ? -playState.getTickPosition() : 0;
		playSong(song != null ? song : gMusicMain.getPlayService().getRandomSong(RADIO_UUID, PlayType.RADIO), delay);

		new MusicGUI(RADIO_UUID, PlayType.RADIO);
	}

	public void playSong(@Nullable Song song) { playSong(song, 0); }

	public void playSong(@Nullable Song song, long delay) {
		if(song == null) return;

		PlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(RADIO_UUID, PlayType.RADIO);

		PlayState playState = gMusicMain.getPlayService().getPlayState(RADIO_UUID);
		if(playState != null) playState.getTimer().cancel();

		Timer timer = new Timer();
		playState = new PlayState(RADIO_UUID, PlayType.RADIO, song, timer, playSettings.isReverseMode() ? song.getLength() + delay : -delay);
		gMusicMain.getPlayService().setPlayState(RADIO_UUID, playState);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) {
			Set<Player> players = new HashSet<>(radioPlayers);
			for(Map.Entry<UUID, Block> radioJukeBox : radioJukeBoxBlocks.entrySet()) {
				PlaySettings jukeBoxPlaySettings = gMusicMain.getPlaySettingsService().getPlaySettings(radioJukeBox.getKey(), PlayType.JUKEBOX);
				HashMap<Player, Double> a = gMusicMain.getJukeBoxService().getPlayersInRange(radioJukeBox.getValue().getLocation().add(0.5, 0, 0.5), jukeBoxPlaySettings.getRange());
				players.addAll(a.keySet());
			}
			for(Player player : players) {
				gMusicMain.getMessageService().sendActionBarMessage(
					player,
					"Messages.actionbar-play",
					"%Song%", song.getId(),
					"%SongTitle%", song.getTitle(),
					"%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(),
					"%OriginalAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-original-author") : song.getOriginalAuthor()
				);
			}
		}

		playTimer(song, timer);
	}

	private void playTimer(@NotNull Song song, @NotNull Timer timer) {
		PlayState playState = gMusicMain.getPlayService().getPlayState(RADIO_UUID);
		PlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(RADIO_UUID, PlayType.RADIO);

		final long[] ticker = {0};

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				ticker[0]++;

				long position = playState.getTickPosition();

				List<NotePart> noteParts = song.getContent().get(position);

				List<Player> players = new ArrayList<>(radioPlayers);

				if(noteParts != null && playSettings.getVolume() > 0 && (!players.isEmpty() || !radioJukeBoxBlocks.isEmpty())) {
					for(Map.Entry<UUID, Block> radioJukeBox : radioJukeBoxBlocks.entrySet()) {
						PlaySettings jukeBoxPlaySettings = gMusicMain.getPlaySettingsService().getPlaySettings(radioJukeBox.getKey(), PlayType.JUKEBOX);
						if(jukeBoxPlaySettings.isShowingParticles()) {
							Location boxLocation = radioJukeBox.getValue().getLocation().add(0.5, 0, 0.5);
							HashMap<Player, Double> playersInRange = gMusicMain.getJukeBoxService().getPlayersInRange(boxLocation, jukeBoxPlaySettings.getRange());
							Location particleLocation = boxLocation.clone().add(random.nextDouble() - 0.5, 1.25, random.nextDouble() - 0.5);
							for(Player player : playersInRange.keySet()) player.spawnParticle(Particle.NOTE, particleLocation, 0, random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
						}
					}

					for(Player player : players) {
						if(player == null) continue;
						PlaySettings playerPlaySettings = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId(), PlayType.DEFAULT);
						if(!playerPlaySettings.isShowingParticles()) continue;
						player.spawnParticle(Particle.NOTE, player.getEyeLocation().clone().add(random.nextDouble() - 0.5, 0.3, random.nextDouble() - 0.5), 0, random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
					}

					for(NotePart notePart : noteParts) {
						for(Player player : players) {
							if(player == null) continue;
							PlaySettings playerPlaySettings = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId(), PlayType.DEFAULT);
							gMusicMain.getMusicUtil().playAtPlayer(player, notePart, playerPlaySettings);
						}

						for(Map.Entry<UUID, Block> radioJukeBox : radioJukeBoxBlocks.entrySet()) {
							PlaySettings jukeBoxPlaySettings = gMusicMain.getPlaySettingsService().getPlaySettings(radioJukeBox.getKey(), PlayType.JUKEBOX);
							Location boxLocation = radioJukeBox.getValue().getLocation().add(0.5, 0, 0.5);
							HashMap<Player, Double> playersInRange = gMusicMain.getJukeBoxService().getPlayersInRange(boxLocation, jukeBoxPlaySettings.getRange());
							for(Player player : playersInRange.keySet()) {
								if(gMusicMain.getConfigService().J_LOCATIONAL_SOUNDS) gMusicMain.getMusicUtil().playAtLocation(player, notePart, boxLocation, playSettings);
								else gMusicMain.getMusicUtil().playAtPlayerWithDecay(player, notePart, playersInRange.get(player), playSettings);
							}
						}
					}
				}

				if(position == (playSettings.isReverseMode() ? 0 : song.getLength())) {
					if(playSettings.getPlayMode() == PlayMode.LOOP) {
						position = playSettings.isReverseMode() ? song.getLength() + gMusicMain.getConfigService().PS_TIME_UNTIL_REPEAT : -gMusicMain.getConfigService().PS_TIME_UNTIL_REPEAT;
						playState.setTickPosition(position);
					} else {
						timer.cancel();
						playSong(gMusicMain.getPlayService().getShuffleSong(RADIO_UUID, song, PlayType.RADIO), gMusicMain.getConfigService().PS_TIME_UNTIL_SHUFFLE);
					}
				} else {
					playState.setTickPosition(playSettings.isReverseMode() ? position - 1 : position + 1);
					if(gMusicMain.getConfigService().A_SHOW_WHILE_PLAYING && ticker[0] % 2000 == 0) {
						for(Player radioPlayer : players) {
							if(radioPlayer == null) continue;
							gMusicMain.getMessageService().sendActionBarMessage(
								radioPlayer,
								"Messages.actionbar-play",
								"%Song%", song.getId(),
								"%SongTitle%", song.getTitle(),
								"%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(),
								"%OriginalAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-original-author") : song.getOriginalAuthor()
							);
						}
					}
				}
			}
		}, 0, 1);
	}

	public Song getNextSong() {
		PlayState playState = gMusicMain.getPlayService().getPlayState(RADIO_UUID);
		return playState != null ? gMusicMain.getPlayService().getShuffleSong(RADIO_UUID, playState.getSong(), PlayType.RADIO) : gMusicMain.getPlayService().getRandomSong(RADIO_UUID, PlayType.RADIO);
	}

	public void stopSong() {
		PlayState playState = gMusicMain.getPlayService().getPlayState(RADIO_UUID);
		if(playState == null) return;

		playState.getTimer().cancel();

		gMusicMain.getPlayService().clearPlayState(RADIO_UUID);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) {
			Set<Player> players = new HashSet<>(radioPlayers);
			for(Map.Entry<UUID, Block> radioJukeBox : radioJukeBoxBlocks.entrySet()) {
				PlaySettings jukeBoxPlaySettings = gMusicMain.getPlaySettingsService().getPlaySettings(radioJukeBox.getKey(), PlayType.JUKEBOX);
				HashMap<Player, Double> a = gMusicMain.getJukeBoxService().getPlayersInRange(radioJukeBox.getValue().getLocation().add(0.5, 0, 0.5), jukeBoxPlaySettings.getRange());
				players.addAll(a.keySet());
			}
			for(Player player : players) {
				gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-pause");
			}
		}
	}

	public void pauseSong() {
		PlayState playState = gMusicMain.getPlayService().getPlayState(RADIO_UUID);
		if(playState == null) return;

		playState.getTimer().cancel();
		playState.setPaused(true);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) {
			Set<Player> players = new HashSet<>(radioPlayers);
			for(Map.Entry<UUID, Block> radioJukeBox : radioJukeBoxBlocks.entrySet()) {
				PlaySettings jukeBoxPlaySettings = gMusicMain.getPlaySettingsService().getPlaySettings(radioJukeBox.getKey(), PlayType.JUKEBOX);
				HashMap<Player, Double> a = gMusicMain.getJukeBoxService().getPlayersInRange(radioJukeBox.getValue().getLocation().add(0.5, 0, 0.5), jukeBoxPlaySettings.getRange());
				players.addAll(a.keySet());
			}
			for(Player player : players) {
				gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-pause");
			}
		}
	}

	public void resumeSong() {
		PlayState playState = gMusicMain.getPlayService().getPlayState(RADIO_UUID);
		if(playState == null) return;

		playState.setTimer(new Timer());
		playState.setPaused(false);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) {
			Set<Player> players = new HashSet<>(radioPlayers);
			for(Map.Entry<UUID, Block> radioJukeBox : radioJukeBoxBlocks.entrySet()) {
				PlaySettings jukeBoxPlaySettings = gMusicMain.getPlaySettingsService().getPlaySettings(radioJukeBox.getKey(), PlayType.JUKEBOX);
				HashMap<Player, Double> a = gMusicMain.getJukeBoxService().getPlayersInRange(radioJukeBox.getValue().getLocation().add(0.5, 0, 0.5), jukeBoxPlaySettings.getRange());
				players.addAll(a.keySet());
			}
			for(Player player : players) {
				gMusicMain.getMessageService().sendActionBarMessage(player, "Messages.actionbar-resume");
			}
		}

		playTimer(playState.getSong(), playState.getTimer());
	}

	public void removeRadioPlayer(@NotNull Player Player) { radioPlayers.remove(Player); }

	public void addRadioPlayer(@NotNull Player Player) { radioPlayers.add(Player); }

	public void removeRadioJukeBox(@NotNull UUID uuid) { radioJukeBoxBlocks.remove(uuid); }

	public void addRadioJukeBox(@NotNull UUID uuid, @NotNull Block block) { radioJukeBoxBlocks.put(uuid, block); }

}