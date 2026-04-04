package dev.geco.gmusic.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum NoteInstrument {

	HARP(0, "block.note_block.harp"),
	BASS(1, "block.note_block.bass"),
	BASEDRUM(2, "block.note_block.basedrum"),
	SNARE(3, "block.note_block.snare"),
	HAT(4, "block.note_block.hat"),
	GUITAR(5, "block.note_block.guitar"),
	FLUTE(6, "block.note_block.flute"),
	BELL(7, "block.note_block.bell"),
	CHIME(8, "block.note_block.chime"),
	XYLOPHONE(9, "block.note_block.xylophone"),
	IRON_XYLOPHONE(10, "block.note_block.iron_xylophone"),
	COW_BELL(11, "block.note_block.cow_bell"),
	DIDGERIDOO(12, "block.note_block.didgeridoo"),
	BIT(13, "block.note_block.bit"),
	BANJO(14, "block.note_block.banjo"),
	PLING(15, "block.note_block.pling"),
	TRUMPET(16, "block.note_block.trumpet"),
	TRUMPET_EXPOSED(17, "block.note_block.trumpet_exposed"),
	TRUMPET_WEATHERED(18, "block.note_block.trumpet_weathered"),
	TRUMPET_OXIDIZED(19, "block.note_block.trumpet_oxidized");

	private final int id;
	private final String sound;

	NoteInstrument(int instrument, String sound) {
		this.id = instrument;
		this.sound = sound;
	}

	public static @Nullable String getIdSound(int id) {
		for(NoteInstrument noteInstrument : values()) if(noteInstrument.id == id) return noteInstrument.sound;
		return null;
	}

	public int getId() {
		return id;
	}

	public @NotNull String getSound() {
		return sound;
	}

}