package dev.geco.gmusic.object;

import dev.geco.gmusic.GMusicMain;

public class GNotePart {

	private static final String PARTS = ":";
	private static final String VAR = "";
	private static final String KEYFLOAT = "#";
	private static final String STOP = "-";
	private final GNote note;
	private String sound;
	private String stopSound;
	private float volume = 1f;
	private float pitch = 1f;
	private int originalPitch = 12;
	private float distance = 0f;

	public GNotePart(GNote note, String notePartString) {
		this.note = note;

		String[] parts = notePartString.split(PARTS);

		if(!parts[0].startsWith(STOP)) sound = this.note.getSong().getInstruments().get(parts[0]);
		else stopSound = this.note.getSong().getInstruments().get(parts[0].replace(STOP, ""));
		if(sound == null || stopSound != null) return;

		if(parts.length == 1 || parts[1].equals(VAR)) {
			volume = 1f;
		} else {
			try { volume = Float.parseFloat(parts[1]) / 100f; } catch(NumberFormatException ignored) { }
		}

		if(parts.length > 2 && !parts[2].equals(VAR)) {
			if(parts[2].contains(KEYFLOAT)) {
				int noteKey = Integer.parseInt(parts[2].replace(KEYFLOAT, ""));
				pitch = getPitch(noteKey);
				originalPitch = getOriginalPitch(noteKey);
				if(GMusicMain.getInstance().getConfigService().S_EXTENDED_RANGE) {
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

	private float getPitch(int note) {
		if(!GMusicMain.getInstance().getConfigService().S_EXTENDED_RANGE) {
			if(note < 0) return 0.5f;
			if(note > 24) return 2f;
			return (float) Math.pow(2, ((float) (note - 12) / 12));
		}
		if(note < -24) {
			note = 36 + note;
		} else if(note < 0) {
			note = 24 + note;
		} else if(note < 48) {
			note = note % 24;
		} else {
			note = 12 + (note % 24);
		}
		return (float) Math.pow(2, ((float) (note - 12) / 12));
	}

	private int getOriginalPitch(int note) {
		if(!GMusicMain.getInstance().getConfigService().S_EXTENDED_RANGE) {
			if(note < 0) return 0;
			return Math.min(note, 24);
		}
		if(note < 0) {
			if(note < -24) note = -24;
			return note;
		}
		if(note > 48) note = 48;
		return note;
	}

	public GNote getNote() { return note; }

	public String getSound() { return sound; }

	public String getStopSound() { return stopSound; }

	public float getVolume() { return volume; }

	public float getPitch() { return pitch; }

	public int getOriginalPitch() { return originalPitch; }

	public float getDistance() { return distance; }

}