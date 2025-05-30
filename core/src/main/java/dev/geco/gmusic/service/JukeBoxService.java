package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.object.GNotePart;
import dev.geco.gmusic.object.GPlayListMode;
import dev.geco.gmusic.object.GPlayMode;
import dev.geco.gmusic.object.GPlaySettings;
import dev.geco.gmusic.object.GPlayState;
import dev.geco.gmusic.object.GSong;
import dev.geco.gmusic.object.gui.GMusicGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.UUID;
import java.util.logging.Level;

public class JukeBoxService {

	private final GMusicMain gMusicMain;
	private final NamespacedKey jukeBoxKey;
	private HashMap<Block, UUID> jukeBoxBlocks = new HashMap<>();
	private HashMap<Block, UUID> radioJukeBoxBlocks = new HashMap<>();
	private final Random random = new Random();

	public JukeBoxService(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
		jukeBoxKey = new NamespacedKey(gMusicMain, GMusicMain.NAME + "_juke_box");
	}

	public void createTables() {
		try {
			gMusicMain.getDataService().execute("CREATE TABLE IF NOT EXISTS gmusic_juke_box (uuid TEXT, world TEXT, x INTEGER, y INTEGER, z INTEGER);");
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not create juke box database tables!", e); }
	}

	public NamespacedKey getJukeBoxKey() { return jukeBoxKey; }

	public UUID getJukeBoxId(Block block) { return jukeBoxBlocks.get(block); }

	public void loadJukeboxes(World world) {
		jukeBoxBlocks.clear();
		radioJukeBoxBlocks.clear();
		gMusicMain.getTaskService().runDelayed(() -> {
			try {
				try(ResultSet jukeBoxData = gMusicMain.getDataService().executeAndGet("SELECT * FROM gmusic_juke_box")) {
					while(jukeBoxData.next()) {
						String worldName = jukeBoxData.getString("world");
						World jukeBoxWorld = Bukkit.getWorld(worldName);
						if(jukeBoxWorld == null || world.equals(jukeBoxWorld)) continue;

						Location location = new Location(jukeBoxWorld, jukeBoxData.getInt("x"), jukeBoxData.getInt("y"), jukeBoxData.getInt("z"));

						UUID uuid = UUID.fromString(jukeBoxData.getString("uuid"));

						Block block = location.getBlock();
						if(block.getType() != Material.JUKEBOX) {
							gMusicMain.getDataService().execute("DELETE FROM gmusic_juke_box WHERE uuid = ?", uuid.toString());
							continue;
						}

						GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);

						jukeBoxBlocks.put(block, uuid);
						if(playSettings.getPlayListMode() == GPlayListMode.RADIO) radioJukeBoxBlocks.put(block, uuid);
						else if(playSettings.isPlayOnJoin()) {
							if(gMusicMain.getPlayService().hasPlayingSong(uuid)) resumeBoxSong(uuid);
							else {
								GSong song = playSettings.getCurrentSong() != null ? gMusicMain.getSongService().getSongById(playSettings.getCurrentSong()) : null;
								playBoxSong(uuid, song != null ? song : gMusicMain.getPlayService().getRandomSong(uuid));
							}
						}

						new GMusicGUI(uuid, GMusicGUI.MenuType.JUKEBOX);
					}
				}
			} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not load jukeboxes", e); }
		}, 0);
	}

	public void setJukebox(Block block) {
		try {
			UUID uuid = UUID.randomUUID();
			gMusicMain.getDataService().execute("INSERT INTO gmusic_juke_box (uuid, world, x, y, z) VALUES (?, ?, ?, ?, ?)",
					uuid.toString(),
					block.getWorld().toString(),
					block.getX(),
					block.getY(),
					block.getZ()
			);
			GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);
			playSettings.setRange(gMusicMain.getConfigService().JUKEBOX_RANGE);
			if(playSettings.getPlayListMode() == GPlayListMode.RADIO) radioJukeBoxBlocks.put(block, uuid);
			jukeBoxBlocks.put(block, uuid);
			new GMusicGUI(uuid, GMusicGUI.MenuType.JUKEBOX);
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not set jukebox", e); }
	}

	public void removeJukebox(Block block) {
		UUID uuid = jukeBoxBlocks.get(block);
		if(uuid == null) return;
		stopBoxSong(uuid);
		gMusicMain.getPlaySettingsService().savePlaySettings(uuid, null);
		GMusicGUI.getMusicGUI(uuid).close(true);
		radioJukeBoxBlocks.remove(block);
		jukeBoxBlocks.remove(block);
		try {
			gMusicMain.getDataService().execute("DELETE FROM gmusic_juke_box WHERE uuid = ?", uuid.toString());
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not remove jukebox", e); }
	}

	public HashMap<Player, Double> getPlayersInRange(Location location, long range) {
		HashMap<Player, Double> playerRangeMap = new HashMap<>();
		if(gMusicMain.getConfigService().WORLDBLACKLIST.contains(location.getWorld().getName())) return playerRangeMap;
		try {
			for(Player player : location.getWorld().getPlayers()) {
				double distance = location.distance(player.getLocation());
				GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId());
				if(playSettings != null && distance <= range && !playSettings.isToggleMode()) playerRangeMap.put(player, distance);
			}
		} catch(Throwable ignored) { }
		return playerRangeMap;
	}

	public void playBoxSong(UUID uuid, GSong song) { playBoxSong(uuid, song, 0); }

	private void playBoxSong(UUID uuid, GSong song, long delay) {
		if(song == null) return;

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);

		GPlayState playState = gMusicMain.getPlayService().getPlayState(uuid);
		if(playState != null) playState.getTimer().cancel();

		Timer timer = new Timer();
		playState = new GPlayState(song, timer, playSettings.isReverseMode() ? song.getLength() + delay : -delay);
		gMusicMain.getPlayService().setPlayState(uuid, playState);

		playSettings.setCurrentSong(song.getId());

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES) {
			//TextComponent anpc = new TextComponent(gMusicMain.getMManager().getMessage("Messages.actionbar-play", "%Title%", Song.getTitle(), "%Author%", Song.getAuthor().equals("") ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-author") : Song.getAuthor(), "%OAuthor%", Song.getOriginalAuthor().equals("") ? gMusicMain.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : Song.getOriginalAuthor()));
			//for(Player P : gMusicMain.getJukeBoxManager().getPlayersInRange(gMusicMain.getValues().getJukeBlocks().get(UUID), playSettings.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}

		playBoxTimer(uuid, song, timer);
	}

	private void playBoxTimer(UUID uuid, GSong song, Timer timer) {
		GPlayState playState = gMusicMain.getPlayService().getPlayState(uuid);

		Location boxLocation = jukeBoxBlocks.entrySet().stream().filter(entry -> entry.getValue().equals(uuid)).findFirst().get().getKey().getLocation();

		gMusicMain.getTaskService().runAtFixedRate(() -> {
			long z = playState.getTickPosition();

			GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);

			if(playSettings != null) {
				List<GNotePart> lnp = song.getContent().get(z);

				HashMap<Player, Double> pl = getPlayersInRange(boxLocation, playSettings.getRange());

				if(lnp != null && playSettings.getVolume() > 0 && !pl.isEmpty()) {
					if(playSettings.isShowingParticles()) {
						Location PL = boxLocation.clone().add(random.nextDouble() - 0.5, 1, random.nextDouble() - 0.5);
						for(Player P : pl.keySet()) P.spawnParticle(Particle.NOTE, PL, 0, random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
					}

					for(GNotePart np : lnp) {
						for(Player P : pl.keySet()) {
							if(np.getSound() != null) {
								float volume = (float) ((pl.get(P) - playSettings.getRange()) * playSettings.getFixedVolume() / (double) -playSettings.getRange()) * np.getVolume();

								Location location = np.getDistance() == 0 ? P.getLocation() : gMusicMain.getSteroNoteUtil().convertToStero(P.getLocation(), np.getDistance());

								if(!gMusicMain.getConfigService().ENVIRONMENT_EFFECTS) P.playSound(location, np.getSound(), song.getSoundCategory(), volume, np.getPitch());
								else {
									if(gMusicMain.getEnvironmentUtil().isPlayerSwimming(P)) P.playSound(location, np.getSound(), song.getSoundCategory(), volume > 0.4f ? volume - 0.3f : volume, np.getPitch() - 0.15f);
									else P.playSound(location, np.getSound(), song.getSoundCategory(), volume, np.getPitch());
								}
							} else if(np.getStopSound() != null) P.stopSound(np.getStopSound(), song.getSoundCategory());
						}
					}
				}

				if(z == (playSettings.isReverseMode() ? 0 : song.getLength())) {
					if(playSettings.getPlayMode() == GPlayMode.LOOP && playSettings.getPlayListMode() != GPlayListMode.RADIO) {
						z = playSettings.isReverseMode() ? song.getLength() + gMusicMain.getConfigService().PS_TIME_UNTIL_REPEAT : -gMusicMain.getConfigService().PS_TIME_UNTIL_REPEAT;
						playState.setTickPosition(z);
					} else {
						timer.cancel();

						if(playSettings.getPlayMode() == GPlayMode.SHUFFLE && playSettings.getPlayListMode() != GPlayListMode.RADIO) playBoxSong(uuid, gMusicMain.getPlayService().getShuffleSong(uuid, song), gMusicMain.getConfigService().PS_TIME_UNTIL_SHUFFLE);
						else {
							gMusicMain.getPlayService().removePlayState(uuid);
							GMusicGUI m = GMusicGUI.getMusicGUI(uuid);
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
		GPlayState playState = gMusicMain.getPlayService().getPlayState(uuid);
		return playState != null ? gMusicMain.getPlayService().getShuffleSong(uuid, playState.getSong()) : gMusicMain.getPlayService().getRandomSong(uuid);
	}

	public void stopBoxSong(UUID uuid) {
		GPlayState playState = gMusicMain.getPlayService().getPlayState(uuid);
		if(playState == null) return;

		playState.getTimer().cancel();

		gMusicMain.getPlayService().removePlayState(uuid);

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);
		playSettings.setCurrentSong(null);

		if(gMusicMain.getConfigService().A_SHOW_MESSAGES && playSettings != null) {
			//TextComponent anpc = new TextComponent(gMusicMain.getMManager().getMessage("Messages.actionbar-stop"));
			//for(Player P : gMusicMain.getJukeBoxManager().getPlayersInRange(gMusicMain.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}
	}

	public void pauseBoxSong(UUID uuid) {
		GPlayState playState = gMusicMain.getPlayService().getPlayState(uuid);
		if(playState == null) return;

		playState.getTimer().cancel();
		playState.setPaused(true);

		GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);
		if(gMusicMain.getConfigService().A_SHOW_MESSAGES && playSettings != null) {
			//TextComponent anpc = new TextComponent(gMusicMain.getMManager().getMessage("Messages.actionbar-pause"));
			//for(Player P : gMusicMain.getJukeBoxManager().getPlayersInRange(gMusicMain.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
		}
	}

	public void resumeBoxSong(UUID uuid) {
		GPlayState playState = gMusicMain.getPlayService().getPlayState(uuid);
		if(playState == null) return;

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