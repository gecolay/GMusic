package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.object.GSong;
import org.bukkit.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

public class SongService {

    private final GMusicMain gMusicMain;
    private static final List<Material> DISCS = Arrays.stream(Material.values()).filter(disc -> disc.name().contains("_DISC_")).toList();
    private final TreeMap<String, GSong> songs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public SongService(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
    }

    public List<GSong> getSongs() { return new ArrayList<>(songs.values()); }

    public GSong getSongById(String song) { return songs.get(song.toLowerCase()); }

    public void loadSongs() {
        unloadSongs();

        convertAllSongs();

        File songsDir = new File(gMusicMain.getDataFolder(), "songs");
        if(!songsDir.exists()) return;

        File[] songFiles = songsDir.listFiles();
        if(songFiles == null) return;
        for(File file : songFiles) {
            int extensionPos = file.getName().lastIndexOf(".");
            if(extensionPos <= 0 || !file.getName().substring(extensionPos + 1).equalsIgnoreCase("gnbs")) return;

            try {
                GSong song = new GSong(file);
                if(song.getNoteAmount() == 0) {
                    gMusicMain.getLogger().warning("Could not load song '" + file.getName().substring(0, extensionPos) + "', no notes found");
                    continue;
                }

                songs.put(song.getId().toLowerCase(), song);
            } catch(Throwable e) {
                gMusicMain.getLogger().log(Level.WARNING, "Could not load song '" + file.getName().substring(0, extensionPos) + "'", e);
            }
        }
    }

    public void unloadSongs() {
        songs.clear();
    }

    private void convertAllSongs() {
        File songsDir = new File(gMusicMain.getDataFolder(), "songs");
        if(!songsDir.exists() && !songsDir.mkdir()) {
            gMusicMain.getLogger().severe("Could not create 'songs' directory!");
            return;
        }

        File nbsDir = new File(gMusicMain.getDataFolder(), "nbs");
        if(!nbsDir.exists() && !nbsDir.mkdir()) {
            gMusicMain.getLogger().severe("Could not create 'nbs' directory!");
            return;
        }

        File midiDir = new File(gMusicMain.getDataFolder(), "midi");
        if(!midiDir.exists() && !midiDir.mkdir()) {
            gMusicMain.getLogger().severe("Could not create 'midi' directory!");
            return;
        }

        File[] nbsFiles = nbsDir.listFiles();
        if(nbsFiles != null) {
            for(File file : nbsFiles) {
                File songFile = new File(songsDir.getAbsolutePath() + "/" + file.getName().replaceFirst("[.][^.]+$", "") + ".gnbs");
                if(songFile.exists()) continue;
                gMusicMain.getNBSConverter().convertNBSFile(file);
            }
        }

        File[] midiFiles = nbsDir.listFiles();
        if(midiFiles == null) return;
        for(File file : midiFiles) {
            File songFile = new File(songsDir.getAbsolutePath() + "/" + file.getName().replaceFirst("[.][^.]+$", "") + ".gnbs");
            if(songFile.exists()) continue;
            gMusicMain.getMidiConverter().convertMidiFile(file);
        }
    }

}