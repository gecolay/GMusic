package dev.geco.gmusic.objects;

public class NotePart {
	
	private final String PARTS = ":";
	
	private final String VAR = "";
	
	private final String KEYFLOAT = "#";
	
	private final String STOP = "-";
	
	
	private final Note n;
	
	
	private String s;
	
	private String ss;
	
	private boolean vv = false;
	
	private float v = 1.0f;
	
	private float p = 1.0f;
	
	private float d = 0;
	
	
	public NotePart(Note Note, String NotePartString) {
		
		n = Note;
		
		String[] a = NotePartString.split(PARTS);
		
		if(!a[0].startsWith(STOP)) s = n.getSong().getInstruments().get(a[0]);
		else ss = n.getSong().getInstruments().get(a[0].replace(STOP, ""));
		if(s == null || ss != null) return;
		
		if(a.length == 1 || a[1].equals(VAR)) vv = true;
		else {
			try { v = Float.parseFloat(a[1]); } catch(NumberFormatException e) { }
		}
		
		if(a.length > 2 && !a[2].equals(VAR)) {
			if(a[2].contains(KEYFLOAT)) p = NotePitch.getPitch(Integer.parseInt(a[2].replace(KEYFLOAT, "")));
			else {
				try { p = Float.parseFloat(a[2]); } catch(NumberFormatException e) { }
			}
		}
		
		if(a.length > 3) d = ((Integer.parseInt(a[3]) - 100) / 200f) * 2f;
		
	}
	
	
	public Note getNote() { return n; }
	
	
	public String getSound() { return s; }
	
	public String getStopSound() { return ss; }
	
	public boolean isVariableVolume() { return vv; }
	
	public float getVolume() { return v; }
	
	public float getPitch() { return p; }
	
	public float getDistance() { return d; }
	
}