package dev.geco.gmusic.objects;

import dev.geco.gmusic.GMusicMain;

public class NotePart {

	private final String PARTS = ":";

	private final String VAR = "";

	private final String KEYFLOAT = "#";

	private final String STOP = "-";

	private final Note note;

	private String sound;

	private String stopSound;

	private boolean variableVolume = false;

	private float volume = 1f;

	private float pitch = 1f;

	private int originalPitch = 12;

	private float distance = 0f;

	public NotePart(Note Note, String NotePartString) {

		note = Note;

		String[] parts = NotePartString.split(PARTS);

		if(!parts[0].startsWith(STOP)) sound = note.getSong().getInstruments().get(parts[0]);
		else stopSound = note.getSong().getInstruments().get(parts[0].replace(STOP, ""));
		if(sound == null || stopSound != null) return;

		if(parts.length == 1 || parts[1].equals(VAR)) variableVolume = true;
		else {
			try { volume = Float.parseFloat(parts[1]); } catch(NumberFormatException ignored) { }
		}

		if(parts.length > 2 && !parts[2].equals(VAR)) {
			if(parts[2].contains(KEYFLOAT)) {
				int noteKey = Integer.parseInt(parts[2].replace(KEYFLOAT, ""));
				pitch = getPitch(noteKey);
				originalPitch = getOriginalPitch(noteKey);
				if(GMusicMain.getInstance().getCManager().S_EXTENDED_RANGE) {
					if(originalPitch >= 24) {
						sound += "_1";
					} else if(originalPitch < 0) {
						sound += "_-1";
					}
				}
			}
			else {
				try { pitch = Float.parseFloat(parts[2]); } catch(NumberFormatException ignored) { }
			}
		}

		if(parts.length > 3) distance = ((Integer.parseInt(parts[3]) - 100) / 200f) * 2f;
	}

	private float getPitch(int Note) {
		if(!GMusicMain.getInstance().getCManager().S_EXTENDED_RANGE) {
			if(Note < 0) return 0.5f;
			if(Note > 24) return 2f;
			return (float) Math.pow(2, ((float) (Note - 12) / 12));
		}

		if (Note < -24) {
			Note = 36 + Note;
		} else if (Note < 0) {
			Note = 24 + Note;
		} else if (Note < 48) {
			Note = Note % 24;
		} else {
			Note = 12 + (Note % 24);
		}
		return (float) Math.pow(2, ((float) (Note - 12) / 12));
	}

	private int getOriginalPitch(int Note) {
		if(!GMusicMain.getInstance().getCManager().S_EXTENDED_RANGE) {
			if(Note < 0) return 0;
			return Math.min(Note, 24);
		}
		if(Note < 0) {
			if(Note < -24) Note = -24;
			return Note;
		}
		if(Note > 48) Note = 48;
		return Note;
	}

	public Note getNote() { return note; }

	public String getSound() { return sound; }

	public String getStopSound() { return stopSound; }

	public boolean isVariableVolume() { return variableVolume; }

	public float getVolume() { return volume; }

	public float getPitch() { return pitch; }

	public int getOriginalPitch() { return originalPitch; }

	public float getDistance() { return distance; }

}