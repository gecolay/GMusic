package dev.geco.gmusic.objects;

import java.util.*;

public class Note {
	
	private final String DELAY = "!";
	
	private final String TICKDELAY = "t";
	
	private final String AMOUNT = ";";
	
	private final String REF = "?";
	
	private final String PARTS = "_";
	
	
	private final Song s;
	
	
	private long d = 0;
	
	private long a = 1;
	
	private List<NotePart> p = new ArrayList<>();
	
	private List<Note> r = new ArrayList<>();
	
	
	public Note(Song Song, String NoteString) {
		
		s = Song;
		
		String ns = NoteString;
		
		if(ns.contains(DELAY)) {
			try {
				d = (ns.contains(TICKDELAY) ? 50 : 1) * Long.parseLong(ns.split(DELAY)[0].replace(TICKDELAY, ""));
				if(d < 0) d = 0;
			} catch(NumberFormatException e) { }
			ns = ns.split(DELAY)[1];
		}
		
		if(ns.contains(AMOUNT)) {
			try {
				long r1 = Long.parseLong(ns.split(AMOUNT)[1]);
				if(r1 > 0) a += r1;
			} catch(NumberFormatException e) { }
			ns = ns.split(AMOUNT)[0];
		}
		
		if(ns.startsWith(REF)) {
			List<Note> p1 = s.getParts().get(ns.replace(REF, ""));
			if(p1 != null) r = p1;
		} else for(String i : ns.split(PARTS)) p.add(new NotePart(this, i));
		
	}
	
	
	public Song getSong() { return s; }
	
	
	public long getDelay() { return d; }
	
	public long getAmount() { return a; }
	
	public List<NotePart> getNoteParts() { return p; }
	
	public List<Note> getReference() { return r; }
	
	public boolean isReference() { return r.size() > 0; }
	
}