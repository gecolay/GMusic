package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.object.GPlaySettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class JukeBoxService {

	private final GMusicMain gMusicMain;

	private File jukeBoxFile;
	private FileConfiguration jukeBoxData;
	private HashMap<UUID, Location> jukeBoxBlocks = new HashMap<>();
	private HashMap<UUID, Location> radioJukeBoxBlocks = new HashMap<>();

	public JukeBoxService(GMusicMain gMusicMain) { this.gMusicMain = gMusicMain; }

	public void generateJukeboxes() {
		jukeBoxBlocks.clear();
		radioJukeBoxBlocks.clear();
		List<String> jbl = jukeBoxData.getStringList("JB");
		List<String> djbl = new ArrayList<>();
		gMusicMain.getTaskService().runDelayed(() -> {
			for(String l : jbl) {
				try {
					String[] a = l.split("/");
					Block B = gMusicMain.getUtilFormat().getStringLocation(a[1]).getBlock();
					if(B.getType() == Material.JUKEBOX) {
						UUID u = UUID.fromString(a[0]);
						GPlaySettings ps = gMusicMain.getPlaySettingsService().getPlaySettings(u);
						gMusicMain.getValues().putPlaySetting(u, ps);
						gMusicMain.getSongService().getMusicGUI(u, MenuType.FULLJUKEBOX);
						B.setMetadata(gMusicMain.NAME + "_JB", new FixedMetadataValue(gMusicMain, u));
						gMusicMain.getValues().putJukeBlock(u, B.getLocation().add(0.5, 0.5, 0.5));
						if(ps.getPlayList() == 2) gMusicMain.getValues().addRadioJukeBox(u);
						else {
							if(ps.isPlayOnJoin()) {
								if(gMusicMain.getPlaySongManager().hasPlayingSong(u)) gMusicMain.getBoxSongManager().resumeBoxSong(u);
								else {
									Song s = ps.getCurrentSong() == null ? gMusicMain.getPlaySongManager().getRandomSong(u) : gMusicMain.getSongManager().getSongById(ps.getCurrentSong());
									gMusicMain.getBoxSongManager().playBoxSong(u, s != null ? s : gMusicMain.getPlaySongManager().getRandomSong(u));
								}
							}
						}
					} else djbl.add(l);
				} catch(Exception e) { e.printStackTrace(); }
			}
			if(!djbl.isEmpty()) {
				for(String l : djbl) jbl.remove(l);
				jukeBoxData.set("JB", jbl);
				saveFile();
			}
		}, 0);
	}

	public void setJukebox(Block block) {
		List<String> jbl = jukeBoxData.getStringList("JB");
		Location L = block.getLocation();
		UUID u = UUID.randomUUID();
		PlaySettings ps = gMusicMain.getPlaySettingsManager().getPlaySettings(u);
		gMusicMain.getPlaySettingsManager().setPlaySettings(u, ps);
		if(ps.getPlayList() == 2) gMusicMain.getRadioManager().addRadioJukeBox(u);
		MusicGUI mgui = gMusicMain.getSongManager().getMusicGUI(u, MenuType.FULLJUKEBOX);
		gMusicMain.getSongManager().putMusicGUI(u, mgui);
		block.setMetadata(gMusicMain.NAME + "_JB", new FixedMetadataValue(gMusicMain, u));
		jbl.add(u.toString() + "/" + gMusicMain.getUtilFormat().getLocationString(L));
		jukeBoxData.set("JB", jbl);
		saveFile();
		putJukeBlock(u, block.getLocation().add(0.5, 0.5, 0.5));
	}

	public void removeJukebox(Block block) {
		List<String> jbl = jukeBoxData.getStringList("JB");
		UUID u = (UUID) block.getMetadata(gMusicMain.NAME + "_JB").get(0).value();
		gMusicMain.getRadioManager().removeRadioJukeBox(u);
		gMusicMain.getBoxSongManager().stopBoxSong(u);
		gMusicMain.getSongManager().getMusicGUIs().get(u).close(true);
		String L = u.toString() + "/" + gMusicMain.getUtilFormat().getLocationString(block.getLocation());
		block.removeMetadata(gMusicMain.NAME + "_JB", gMusicMain);
		jbl.remove(L);
		jukeBoxData.set("JB", jbl);
		gMusicMain.getPlaySettingsManager().setPlaySettings(u, null);
		saveFile();
		gMusicMain.getValues().removeJukeBlock(u);
		gMusicMain.getValues().removeMusicGUI(u);
		gMusicMain.getValues().removePlaySetting(u);
	}

	public void removeTempJukebox(Block block) {
		PlaySettings ps = getTempJukeBlocks().get(block);
		if(ps != null) {
			gMusicMain.getBoxSongManager().stopBoxSong(ps.getUUID());
			gMusicMain.getValues().removeJukeBlock(ps.getUUID());
			gMusicMain.getValues().removeTempJukeBlock(block);
			gMusicMain.getValues().removePlaySetting(ps.getUUID());
		}
	}

	public HashMap<Player, Double> getPlayersInRange(Location L, long Range) {
		HashMap<Player, Double> pl = new HashMap<Player, Double>();
		if(gMusicMain.getCManager().WORLDBLACKLIST.contains(L.getWorld().getName())) return pl;
		try {
			for(Player t : L.getWorld().getPlayers()) {
				double d = L.distance(t.getLocation());
				PlaySettings t1 = gMusicMain.getPlaySettingsManager().getPlaySettings(t.getUniqueId());
				if(t1 != null && d <= Range && !t1.isToggleMode()) pl.put(t, d);
			}
		} catch(ConcurrentModificationException | NullPointerException e) {}
		return pl;
	}

	public void reloadFile() {
		jukeBoxFile = new File("plugins/" + gMusicMain.NAME, Values.DATA_PATH + "/" + Values.JUKEBOX_FILE + Values.DATA_FILETYP);
		jukeBoxData = YamlConfiguration.loadConfiguration(jukeBoxFile);
	}

	private void saveFile() { try { jukeBoxData.save(jukeBoxFile); } catch (IOException e) { } }

}