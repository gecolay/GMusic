package dev.geco.gmusic.object;

import java.util.ArrayList;
import java.util.List;

public class GNote {

	/*
	 tickdelay!
	 ;amount
	 ?ref
	 -stop
	 _parts
	 (instrument : volume : pitch (#X) : ?distance) _ (instrument : volume : pitch (#X) : ?distance)
	 */

	private static final String DELAY = "!";
	private static final String TICKDELAY = "t";
	private static final String AMOUNT = ";";
	private static final String REF = "?";
	private static final String PARTS = "_";
	private final GSong song;
	private long delay = 0;
	private long amount = 1;
	private final List<GNotePart> parts = new ArrayList<>();
	private List<GNote> references = new ArrayList<>();

	public GNote(GSong song, String noteString) {
		this.song = song;

		if(noteString.contains(DELAY)) {
			try {
				delay = (noteString.contains(TICKDELAY) ? 50 : 1) * Long.parseLong(noteString.split(DELAY)[0].replace(TICKDELAY, ""));
				if(delay < 0) delay = 0;
			} catch(NumberFormatException ignored) { }
			noteString = noteString.split(DELAY)[1];
		}

		if(noteString.contains(AMOUNT)) {
			try {
				long nodeAmount = Long.parseLong(noteString.split(AMOUNT)[1]);
				if(nodeAmount > 0) amount += nodeAmount;
			} catch(NumberFormatException ignored) { }
			noteString = noteString.split(AMOUNT)[0];
		}

		if(noteString.startsWith(REF)) {
			List<GNote> noteReferences = this.song.getParts().get(noteString.replace(REF, ""));
			if(noteReferences != null) references = noteReferences;
		} else for(String i : noteString.split(PARTS)) parts.add(new GNotePart(this, i));
	}

	public GSong getSong() { return song; }

	public long getDelay() { return delay; }

	public long getAmount() { return amount; }

	public List<GNotePart> getNoteParts() { return parts; }

	public List<GNote> getReferences() { return references; }

	public boolean isReference() { return !references.isEmpty(); }

}