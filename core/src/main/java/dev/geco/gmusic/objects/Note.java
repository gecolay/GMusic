package dev.geco.gmusic.objects;

import java.util.*;

public class Note {

	/*
	 delay(t)!
	 ;amount
	 ?ref
	 -stop
	 _parts
	 (instrument : volume : pitch (#X) : ?distance) _ (instrument : volume : pitch (#X) : ?distance)
	 */

	private final String DELAY = "!";

	private final String TICKDELAY = "t";

	private final String AMOUNT = ";";

	private final String REF = "?";

	private final String PARTS = "_";

	private final Song song;

	private long delay = 0;

	private long amount = 1;

	private final List<NotePart> parts = new ArrayList<>();

	private List<Note> references = new ArrayList<>();

	public Note(Song Song, String NoteString) {

		song = Song;

		if(NoteString.contains(DELAY)) {
			try {
				delay = (NoteString.contains(TICKDELAY) ? 50 : 1) * Long.parseLong(NoteString.split(DELAY)[0].replace(TICKDELAY, ""));
				if(delay < 0) delay = 0;
			} catch(NumberFormatException ignored) { }
			NoteString = NoteString.split(DELAY)[1];
		}

		if(NoteString.contains(AMOUNT)) {
			try {
				long nodeAmount = Long.parseLong(NoteString.split(AMOUNT)[1]);
				if(nodeAmount > 0) amount += nodeAmount;
			} catch(NumberFormatException ignored) { }
			NoteString = NoteString.split(AMOUNT)[0];
		}

		if(NoteString.startsWith(REF)) {
			List<Note> noteReferences = song.getParts().get(NoteString.replace(REF, ""));
			if(noteReferences != null) references = noteReferences;
		} else for(String i : NoteString.split(PARTS)) parts.add(new NotePart(this, i));
	}

	public Song getSong() { return song; }

	public long getDelay() { return delay; }

	public long getAmount() { return amount; }

	public List<NotePart> getNoteParts() { return parts; }

	public List<Note> getReferences() { return references; }

	public boolean isReference() { return !references.isEmpty(); }

}