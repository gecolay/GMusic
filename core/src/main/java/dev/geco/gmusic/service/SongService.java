package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.object.GSong;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

public class SongService {

    private final GMusicMain gMusicMain;
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

        for(File file : songsDir.listFiles()) {
            int extensionPos = file.getName().lastIndexOf(".");
            if(extensionPos <= 0 || !file.getName().substring(extensionPos + 1).equalsIgnoreCase("gnbs")) return;

            try {
                GSong song = new GSong(file);
                if(song.getNoteAmount() == 0) return;

                List<String> description = new ArrayList<>();
                for(String descriptionRow : song.getDescription()) description.add(gMusicMain.getMessageService().getMessage(descriptionRow));

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
        if(!songsDir.exists()) songsDir.mkdir();

        File nbsDir = new File(gMusicMain.getDataFolder(), "nbs");
        if(!nbsDir.exists()) nbsDir.mkdir();

        File midiDir = new File(gMusicMain.getDataFolder(), "midi");
        if(!midiDir.exists()) midiDir.mkdir();

        for(File file : nbsDir.listFiles()) {
            File songFile = new File(songsDir.getAbsolutePath() + "/" + file.getName().replaceFirst("[.][^.]+$", "") + ".gnbs");
            if(songFile.exists()) continue;
            gMusicMain.getNBSService().convertNBSFile(file);
        }

        for(File file : midiDir.listFiles()) {
            File songFile = new File(songsDir.getAbsolutePath() + "/" + file.getName().replaceFirst("[.][^.]+$", "") + ".gnbs");
            if(songFile.exists()) continue;
            gMusicMain.getMidiService().convertMidiFile(file);
        }
    }

}