package dev.geco.gmusic.objects;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import org.bukkit.*;
import org.bukkit.configuration.file.*;

public class Song {
	
	private YamlConfiguration f;
	
	private final String fn;
	
	private final String id;
	
	private String t;
	
	private String o;
	
	private String a;
	
	private List<String> d = new ArrayList<>();
	
	private List<String> cg = new ArrayList<>();
	
	private Material ma;
	
	private SoundCategory c;
	
	
	private HashMap<String, String> i = new HashMap<String, String>();
	
	private HashMap<String, List<Note>> p = new HashMap<String, List<Note>>();
	
	private List<Note> m = new ArrayList<>();
	
	
	private HashMap<Long, List<NotePart>> co = new HashMap<Long, List<NotePart>>();
	
	private long na = 0;
	
	private long e = 0;
	
	
	private final List<Material> DICS = Tag.ITEMS_MUSIC_DISCS.getValues().parallelStream().collect(Collectors.toList());
	
	public Song(File File) {
		
		f = YamlConfiguration.loadConfiguration(File);
		fn = File.getName();
		
		id = f.getString("Song.Id");
		t = f.getString("Song.Title", id);
		o = f.getString("Song.OAuthor");
		a = f.getString("Song.Author");
		d = f.getStringList("Song.Description");
		cg = f.getStringList("Song.Categorys");
		String tm = f.getString("Song.Material");
		if(tm != null) {
			try { ma = Material.valueOf(tm.toUpperCase()); } catch(IllegalArgumentException e) { }
		}
		if(ma == null) ma = id == null ? DICS.get(0) : DICS.get(id.length() <= DICS.size() - 1 ? id.length() : id.length() % (DICS.size() - 1));
		try { c = SoundCategory.valueOf(f.getString("Song.Category").toUpperCase()); } catch(IllegalArgumentException e) { c = SoundCategory.RECORDS; }
		
		List<String> il = new ArrayList<>();
		try { for(String l : f.getConfigurationSection("Song.Content.Instruments").getKeys(false)) il.add(l); } catch (Exception e) { }
		for(String l : il) {
			try {
				String s = NoteInstrument.getInstrument(Integer.parseInt(f.getString("Song.Content.Instruments." + l)));
				if(s != null) i.put(l, s);
				else throw new NumberFormatException();
			} catch(IllegalArgumentException e) { i.put(l, f.getString("Song.Content.Instruments." + l)); }
		}
		
		List<String> pl = new ArrayList<>();
		try { for(String l : f.getConfigurationSection("Song.Content.Parts").getKeys(false)) pl.add(l); } catch (Exception e) { }
		
		for(String l : pl) {
			List<Note> pl1 = new ArrayList<>();
			for(String l1 : f.getStringList("Song.Content.Parts." + l)) pl1.add(new Note(this, l1));
			p.put(l, pl1);
		}
		
		List<String> ml = f.getStringList("Song.Content.Main");
		for(String l : ml) m.add(new Note(this, l));
		
		for(Note n : m) {
			
			if(n.isReference()) {
				
				for(long z = 1; z <= n.getAmount(); z++) {
					
					e += n.getDelay();
					
					for(Note n1 : n.getReference()) {
						
						for(long z1 = 1; z1 <= n1.getAmount(); z1++) {
							
							e += n1.getDelay();
							
							if(co.containsKey(e)) {
								List<NotePart> np = co.get(e);
								np.addAll(n1.getNoteParts());
								co.put(e, np);
							} else co.put(e, n1.getNoteParts());
							
							na += n1.getNoteParts().size();
							
						}
						
					}
					
				}
				
			} else {
				
				for(long z = 1; z <= n.getAmount(); z++) {
					
					e += n.getDelay();
					
					if(co.containsKey(e)) {
						List<NotePart> np = co.get(e);
						np.addAll(n.getNoteParts());
						co.put(e, np);
					} else co.put(e, n.getNoteParts());
					
					na += n.getNoteParts().size();
					
				}
				
			}
			
		}
		
		f = null;
		
	}
	
	
	public String getFileName() { return fn; }
	
	
	public String getId() { return id; }
	
	public String getTitle() { return t; }
	
	public String getOriginalAuthor() { return o; }
	
	public String getAuthor() { return a; }
	
	public List<String> getDescription() { return d; }
	
	public List<String> getCategorys() { return cg; }
	
	public Material getMaterial() { return ma; }
	
	public SoundCategory getCategory() { return c; }
	
	
	public HashMap<String, String> getInstruments() { return i; }
	
	public HashMap<String, List<Note>> getParts() { return p; }
	
	public List<Note> getMain() { return m; }
	
	
	public HashMap<Long, List<NotePart>> getContent() { return co; }
	
	public long getStepAmount() { return co.size(); }
	
	public long getNoteAmount() { return na; }
	
	public long getLength() { return e; }
	
}