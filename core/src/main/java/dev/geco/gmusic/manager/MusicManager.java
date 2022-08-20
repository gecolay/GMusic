package dev.geco.gmusic.manager;

import java.io.*;
import java.util.*;

import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.*;
import dev.geco.gmusic.objects.MusicGUI.MenuType;
import dev.geco.gmusic.values.Values;

public class MusicManager {
	
	private final GMusicMain GPM;
	
	private ItemStack i;
	
    public MusicManager(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
    public void convertSongs() {
    	
    	File p = new File("plugins/" + GPM.NAME + "/" + Values.SONGS_PATH);
    	
    	if(!p.exists()) p.mkdir();
    	
    	File p1 = new File("plugins/" + GPM.NAME + "/" + Values.CONVERT_PATH);
    	
    	if(!p1.exists()) p1.mkdir();
    	
    	File p2 = new File("plugins/" + GPM.NAME + "/" + Values.MIDI_PATH);
    	
    	if(!p2.exists()) p2.mkdir();
    	
    	Arrays.asList(p1.listFiles()).parallelStream().forEach(f -> {
    		
    		if(!new File(p.getAbsolutePath() + "/" + f.getName().replaceFirst("[.][^.]+$", "") + Values.GNBS_FILETYP).exists()) GPM.getNBSManager().convertFile(f);
    		
    	});
    	
    	Arrays.asList(p2.listFiles()).parallelStream().forEach(f -> {
    		
    		if(!new File(p.getAbsolutePath() + "/" + f.getName().replaceFirst("[.][^.]+$", "") + Values.GNBS_FILETYP).exists()) GPM.getMidiManager().convertFile(f);
    		
    	});
    	
    }
    
    public ItemStack getJukeBox() { return i; }
    
    public void loadMusicSettings() {
    	
    	GPM.getValues().clearSongs();
    	
    	GPM.getValues().clearDiscItems();
    	
    	File p = new File("plugins/" + GPM.NAME + "/" + Values.SONGS_PATH);
    	
    	if(!p.exists()) p.mkdir();
    	
    	try {
    		
    		Arrays.asList(p.listFiles()).parallelStream().forEach(f -> {
        		
        		int i = f.getName().lastIndexOf(".");
        		if(i > 0 && f.getName().substring(i + 1).equalsIgnoreCase(Values.GNBS_EXT)) {
        			
        			Song s = new Song(f);
        			
        			if(s.getNoteAmount() == 0) return;
        			
        			ItemStack is = new ItemStack(s.getMaterial());
        			
        			ItemMeta im = is.getItemMeta();
        			
        			im.setDisplayName(GPM.getMManager().getMessage("Items.disc-title", "%Title%", s.getTitle(), "%Author%", s.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : s.getAuthor(), "%OAuthor%", s.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : s.getOriginalAuthor()));
        			
        			im.setLocalizedName(GPM.NAME + "_D_" + s.getId());
        			
        			List<String> dl = new ArrayList<>();
        			
        			for(String d : s.getDescription()) dl.add(GPM.getMManager().getColoredMessage("&6" + d));
        			
        			im.setLore(dl);
        			
        			im.addItemFlags(ItemFlag.values());
        			
        			is.setItemMeta(im);
        			
        			GPM.getValues().putDiscItem(is, s);
        			
        			GPM.getValues().addSong(s);
        			
        		}
        		
        	});
    		
    	} catch(Exception | Error e) { e.printStackTrace(); }
    	
    	GPM.getValues().sortSongs();
    	
    	i = new ItemStack(Material.JUKEBOX);
    	ItemMeta im = i.getItemMeta();
    	im.setDisplayName(GPM.getMManager().getMessage("Items.jukebox-title"));
    	im.setLocalizedName(GPM.NAME + "_JB");
    	List<String> iml = new ArrayList<>();
    	for(String imlr : GPM.getMManager().getMessage("Items.jukebox-description").split("\n")) iml.add(imlr);
    	im.setLore(iml);
    	i.setItemMeta(im);
    	
    }
    
    public MusicGUI getMusicGUI(UUID U, MenuType MenuType) {
    	
    	MusicGUI r = GPM.getValues().getMusicGUIs().get(U);
    	
    	return r != null ? r : new MusicGUI(U, MenuType, GPM);
    	
    }
    
}