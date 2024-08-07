package dev.geco.gmusic.manager;

import java.io.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.metadata.*;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.objects.*;
import dev.geco.gmusic.objects.MusicGUI.*;

public class JukeBoxManager {

	private final GMusicMain GPM;

	private File JukeBData;
	private FileConfiguration JukeBD;
	private HashMap<UUID, Location> jukeBoxBlock = new HashMap<>();

	public JukeBoxManager(GMusicMain GPluginMain) { GPM = GPluginMain; }

	public void generateJukeboxes() {
		/*GPM.getValues().clearJukeBlocks();
		GPM.getValues().clearRadioJukeBox();
		List<String> jbl = JukeBD.getStringList("JB");
		List<String> djbl = new ArrayList<>();
		GPM.getTManager().runDelayed(() -> {
			for(String l : jbl) {
				try {
					String[] a = l.split("/");
					Block B = GPM.getUtilFormat().getStringLocation(a[1]).getBlock();
					if(B.getType() == Material.JUKEBOX) {
						UUID u = UUID.fromString(a[0]);
						PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(u);
						GPM.getValues().putPlaySetting(u, ps);
						GPM.getSongManager().getMusicGUI(u, MenuType.FULLJUKEBOX);
						B.setMetadata(GPM.NAME + "_JB", new FixedMetadataValue(GPM, u));
						GPM.getValues().putJukeBlock(u, B.getLocation().add(0.5, 0.5, 0.5));
						if(ps.getPlayList() == 2) GPM.getValues().addRadioJukeBox(u);
						else {
							if(ps.isPlayOnJoin()) {
								if(GPM.getPlaySongManager().hasPlayingSong(u)) GPM.getBoxSongManager().resumeBoxSong(u);
								else {
									Song s = ps.getCurrentSong() == null ? GPM.getPlaySongManager().getRandomSong(u) : GPM.getSongManager().getSongById(ps.getCurrentSong());
									GPM.getBoxSongManager().playBoxSong(u, s != null ? s : GPM.getPlaySongManager().getRandomSong(u));
								}
							}
						}
					} else djbl.add(l);
				} catch(Exception e) { e.printStackTrace(); }
			}
			if(!djbl.isEmpty()) {
				for(String l : djbl) jbl.remove(l);
				JukeBD.set("JB", jbl);
				saveFile();
			}
		}, 0);*/
	}

	/*public void setNewJukebox(Block B) {
		List<String> jbl = JukeBD.getStringList("JB");
		Location L = B.getLocation();
		UUID u = UUID.randomUUID();
		PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(u);
		GPM.getPlaySettingsManager().setPlaySettings(u, ps);
		if(ps.getPlayList() == 2) GPM.getRadioManager().addRadioJukeBox(u);
		MusicGUI mgui = GPM.getSongManager().getMusicGUI(u, MenuType.FULLJUKEBOX);
		GPM.getSongManager().putMusicGUI(u, mgui);
		B.setMetadata(GPM.NAME + "_JB", new FixedMetadataValue(GPM, u));
		jbl.add(u.toString() + "/" + GPM.getUtilFormat().getLocationString(L));
		JukeBD.set("JB", jbl);
		saveFile();
		putJukeBlock(u, B.getLocation().add(0.5, 0.5, 0.5));
	}

	public void removeJukebox(Block B) {
		List<String> jbl = JukeBD.getStringList("JB");
		UUID u = (UUID) B.getMetadata(GPM.NAME + "_JB").get(0).value();
		GPM.getRadioManager().removeRadioJukeBox(u);
		GPM.getBoxSongManager().stopBoxSong(u);
		GPM.getSongManager().getMusicGUIs().get(u).close(true);
		String L = u.toString() + "/" + GPM.getUtilFormat().getLocationString(B.getLocation());
		B.removeMetadata(GPM.NAME + "_JB", GPM);
		jbl.remove(L);
		JukeBD.set("JB", jbl);
		GPM.getPlaySettingsManager().setPlaySettings(u, null);
		saveFile();
		GPM.getValues().removeJukeBlock(u);
		GPM.getValues().removeMusicGUI(u);
		GPM.getValues().removePlaySetting(u);
	}

	public void removeTempJukebox(Block B) {
		PlaySettings ps = getTempJukeBlocks().get(B);
		if(ps != null) {
			GPM.getBoxSongManager().stopBoxSong(ps.getUUID());
			GPM.getValues().removeJukeBlock(ps.getUUID());
			GPM.getValues().removeTempJukeBlock(B);
			GPM.getValues().removePlaySetting(ps.getUUID());
		}
	}

	public HashMap<Player, Double> getPlayersInRange(Location L, long Range) {
		HashMap<Player, Double> pl = new HashMap<Player, Double>();
		if(GPM.getCManager().WORLDBLACKLIST.contains(L.getWorld().getName())) return pl;
		try {
			for(Player t : L.getWorld().getPlayers()) {
				double d = L.distance(t.getLocation());
				PlaySettings t1 = GPM.getPlaySettingsManager().getPlaySettings(t.getUniqueId());
				if(t1 != null && d <= Range && !t1.isToggleMode()) pl.put(t, d);
			}
		} catch(ConcurrentModificationException | NullPointerException e) {}
		return pl;
	}

	public void reloadFile() {
		JukeBData = new File("plugins/" + GPM.NAME, Values.DATA_PATH + "/" + Values.JUKEBOX_FILE + Values.DATA_FILETYP);
		JukeBD = YamlConfiguration.loadConfiguration(JukeBData);
	}

	private void saveFile() { try { JukeBD.save(JukeBData); } catch (IOException e) { } }*/

}