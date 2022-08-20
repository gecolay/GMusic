package dev.geco.gmusic.values;

import java.util.*;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import dev.geco.gmusic.objects.*;

public class Values {
	
	private boolean papi = false;
	
	public boolean getPAPI() { return papi; }
	
	public void setPAPI(boolean PAPI) { papi = PAPI; }
	
	private List<Song> songs = new ArrayList<>();
	
	public List<Song> getSongs() { return songs; }
	
	public void sortSongs() { Collections.sort(songs, Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER).thenComparing(Song::getTitle)); }
	
	public void addSong(Song S) { songs.add(S); }
	
	public void clearSongs() { songs.clear(); }
	
	private HashMap<ItemStack, Song> disci = new HashMap<ItemStack, Song>();
	
	public HashMap<ItemStack, Song> getDiscItems() { return disci; }
	
	public void putDiscItem(ItemStack I, Song S) { disci.put(I, S); }
	
	public void clearDiscItems() { disci.clear(); }
	
	private List<Player> radiop = new ArrayList<>();
	
	public List<Player> getRadioPlayers() { return radiop; }
	
	public void addRadioPlayer(Player P) { radiop.add(P); }
	
	public void removeRadioPlayer(Player P) { radiop.remove(P); }
	
	public void clearRadioPlayers() { radiop.clear(); }
	
	private List<UUID> radioj = new ArrayList<>();
	
	public List<UUID> getRadioJukeBoxes() { return radioj; }
	
	public void addRadioJukeBox(UUID U) { radioj.add(U); }
	
	public void removeRadioJukeBox(UUID U) { radioj.remove(U); }
	
	public void clearRadioJukeBox() { radioj.clear(); }
	
	
	private HashMap<UUID, MusicGUI> musicgui = new HashMap<UUID, MusicGUI>();
	
	public HashMap<UUID, MusicGUI> getMusicGUIs() { return musicgui; }
	
	public void putMusicGUI(UUID U, MusicGUI M) { musicgui.put(U, M); }
	
	public void removeMusicGUI(UUID U) { musicgui.remove(U); }
	
	public void clearMusicGUIs() { musicgui.clear(); }
	
	private HashMap<Player, SearchGUI> inputgui = new HashMap<Player, SearchGUI>();
	
	public HashMap<Player, SearchGUI> getInputGUIs() { return inputgui; }
	
	public void putInputGUI(Player P, SearchGUI I) { inputgui.put(P, I); }
	
	public void removeInputGUI(Player P) { inputgui.remove(P); }
	
	public void clearInputGUIs() { inputgui.clear(); }
	
	private HashMap<UUID, SongSettings> songt = new HashMap<UUID, SongSettings>();
	
	public HashMap<UUID, SongSettings> getSongSettings() { return songt; }
	
	public void putSongSettings(UUID U, SongSettings SS) { songt.put(U, SS); }
	
	public void removeSongSettings(UUID U) { songt.remove(U); }
	
	public void clearSongSettings() { songt.clear(); }
	
	private HashMap<UUID, PlaySettings> pset = new HashMap<UUID, PlaySettings>();
	
	public HashMap<UUID, PlaySettings> getPlaySettings() { return pset; }
	
	public void putPlaySetting(UUID U, PlaySettings PS) { pset.put(U, PS); }
	
	public void removePlaySetting(UUID U) { pset.remove(U); }
	
	public void clearPlaySettings() { pset.clear(); }
	
	private HashMap<UUID, Location> jbs = new HashMap<UUID, Location>();
	
	public HashMap<UUID, Location> getJukeBlocks() { return jbs; }
	
	public void putJukeBlock(UUID U, Location L) { jbs.put(U, L); }
	
	public void removeJukeBlock(UUID U) { jbs.remove(U); }
	
	public void clearJukeBlocks() { jbs.clear(); }
	
	private HashMap<Block, PlaySettings> tjbs = new HashMap<Block, PlaySettings>();
	
	public HashMap<Block, PlaySettings> getTempJukeBlocks() { return tjbs; }
	
	public void putTempJukeBlock(Block B, PlaySettings PS) { tjbs.put(B, PS); }
	
	public void removeTempJukeBlock(Block B) { tjbs.remove(B); }
	
	public void clearTempJukeBlocks() { tjbs.clear(); }
	
	
	public static final long VOLUME_STEPS = 10;
	
	public static final long RANGE_STEPS = 1;
	
	public static final long SHIFT_RANGE_STEPS = 10;
	
	public static final String PLAYERS_ALL = "@a";
	
	public static final String JUKEBOX_FILE = "jukebox";
	
	public static final String PLAY_FILE = "play";
	
	public static final String MIDI_PATH = "midi";
	
	public static final String SONGS_PATH = "songs";
	
	public static final String CONVERT_PATH = "convert";
	
	public static final String DATA_PATH = "data";
	
	public static final String LANG_PATH = "lang";
	
	public static final String NBS_EXT = "nbs";
	
	public static final String GNBS_EXT = "gnbs";
	
	public static final String MIDI_EXT = "midi";
	
	public static final String NBS_FILETYP = ".nbs";
	
	public static final String GNBS_FILETYP = ".gnbs";
	
	public static final String MID_FILETYP = ".mid";
	
	public static final String DATA_FILETYP = ".data";
	
	public static final String YML_FILETYP = ".yml";
	
}