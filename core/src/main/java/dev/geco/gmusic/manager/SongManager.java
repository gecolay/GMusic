package dev.geco.gmusic.manager;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.objects.*;

public class SongManager {

    private final GMusicMain GPM;

    public SongManager(GMusicMain GPluginMain) { GPM = GPluginMain; }

    private final List<Song> songs = new ArrayList<>();

    public List<Song> getSongs() { return new ArrayList<>(songs); }

    public Song getSongById(String Song) { return songs.parallelStream().filter(song -> song.getId().equalsIgnoreCase(Song)).findFirst().orElse(null); }

    public List<Song> getSongsBySearch(String Search) { return songs.parallelStream().filter(song -> song.getTitle().toLowerCase().contains(Search.toLowerCase())).collect(Collectors.toList()); }

    public void convertAllSongs() {

        File songsDir = new File(GPM.getDataFolder(), "songs");
        if(!songsDir.exists()) songsDir.mkdir();

        File nbsDir = new File(GPM.getDataFolder(), "nbs");
        if(!nbsDir.exists()) nbsDir.mkdir();

        File midiDir = new File(GPM.getDataFolder(), "midi");
        if(!midiDir.exists()) midiDir.mkdir();

        Arrays.asList(Objects.requireNonNull(nbsDir.listFiles())).parallelStream().forEach(file -> {

            if(!new File(songsDir.getAbsolutePath() + "/" + file.getName().replaceFirst("[.][^.]+$", "") + ".gnbs").exists()) GPM.getNBSManager().convertFile(file);
        });

        Arrays.asList(Objects.requireNonNull(midiDir.listFiles())).parallelStream().forEach(file -> {

            if(!new File(songsDir.getAbsolutePath() + "/" + file.getName().replaceFirst("[.][^.]+$", "") + ".gnbs").exists()) GPM.getMidiManager().convertFile(file);
        });
    }

    public void loadSongs() {

        songs.clear();

        convertAllSongs();

        File songsDir = new File(GPM.getDataFolder(), "songs");

        Arrays.asList(Objects.requireNonNull(songsDir.listFiles())).parallelStream().forEach(file -> {

            int pos = file.getName().lastIndexOf(".");
            if(pos <= 0 || !file.getName().substring(pos + 1).equalsIgnoreCase("gnbs")) return;

            Song song = new Song(file);

            if(song.getNoteAmount() == 0) return;

            List<String> description = new ArrayList<>();
            for(String descriptionRow : song.getDescription()) description.add(GPM.getMManager().getMessage(descriptionRow));

            ItemStack itemStack = new ItemStack(song.getMaterial());
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(GPM.getMManager().getMessage("Items.disc-title", "%Title%", song.getTitle(), "%Author%", song.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(), "%OAuthor%", song.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : song.getOriginalAuthor()));
            itemMeta.setLocalizedName(GPM.NAME + "_D_" + song.getId());
            itemMeta.setLore(description);
            itemMeta.addItemFlags(ItemFlag.values());
            itemStack.setItemMeta(itemMeta);

            songs.add(song);
        });
    }

}