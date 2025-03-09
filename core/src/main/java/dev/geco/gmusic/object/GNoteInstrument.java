package dev.geco.gmusic.object;

public enum GNoteInstrument {

	INST_0(0, "block.note_block.harp"),
	INST_1(1, "block.note_block.bass"),
	INST_2(2, "block.note_block.basedrum"),
	INST_3(3, "block.note_block.snare"),
	INST_4(4, "block.note_block.hat"),
	INST_5(5, "block.note_block.guitar"),
	INST_6(6, "block.note_block.flute"),
	INST_7(7, "block.note_block.bell"),
	INST_8(8, "block.note_block.chime"),
	INST_9(9, "block.note_block.xylophone"),
	INST_10(10, "block.note_block.iron_xylophone"),
	INST_11(11, "block.note_block.cow_bell"),
	INST_12(12, "block.note_block.didgeridoo"),
	INST_13(13, "block.note_block.bit"),
	INST_14(14, "block.note_block.banjo"),
	INST_15(15, "block.note_block.pling");

	private final int instrument;
	private final String sound;

	GNoteInstrument(int instrument, String sound) {
		this.instrument = instrument;
		this.sound = sound;
	}

	public static String getInstrument(int instrument) {
		for(GNoteInstrument noteInstrument : values()) if(noteInstrument.instrument == instrument) return noteInstrument.sound;
		return null;
	}

}