package dev.geco.gmusic.service.converter;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.model.NoteInstrument;
import dev.geco.gmusic.service.SongService;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

public class MidiConverter {

	private static final int MIDI_CHANNELS = 16;
	private static final int DRUM_CHANNEL = 9;

	private final GMusicMain gMusicMain;

	private final Map<Integer, MidiPair> MIDI_INST = new HashMap<>() {{
		// Piano
		put(0, new MidiPair(NoteInstrument.HARP, 0));
		put(1, new MidiPair(NoteInstrument.HARP, 0));
		put(2, new MidiPair(NoteInstrument.PLING, 0));
		put(3, new MidiPair(NoteInstrument.HARP, 0));
		put(4, new MidiPair(NoteInstrument.BIT, 0));
		put(5, new MidiPair(NoteInstrument.BIT, 0));
		put(6, new MidiPair(NoteInstrument.HARP, 1));
		put(7, new MidiPair(NoteInstrument.HARP, 0));

		// Chromatic percussion
		put(8, new MidiPair(NoteInstrument.COW_BELL, -1));
		put(9, new MidiPair(NoteInstrument.COW_BELL, 0));
		put(10, new MidiPair(NoteInstrument.COW_BELL, 0));
		put(11, new MidiPair(NoteInstrument.IRON_XYLOPHONE, 0));
		put(12, new MidiPair(NoteInstrument.COW_BELL, 0));
		put(13, new MidiPair(NoteInstrument.XYLOPHONE, 0));
		put(14, new MidiPair(NoteInstrument.BELL, -1));
		put(15, new MidiPair(NoteInstrument.BELL, 0));

		// Organ
		put(16, new MidiPair(NoteInstrument.BASS, 1));
		put(17, new MidiPair(NoteInstrument.BASS, 1));
		put(18, new MidiPair(NoteInstrument.HARP, 0));
		put(19, new MidiPair(NoteInstrument.HARP, 0));
		put(20, new MidiPair(NoteInstrument.HARP, 0));
		put(21, new MidiPair(NoteInstrument.HARP, 0));
		put(22, new MidiPair(NoteInstrument.HARP, 0));
		put(23, new MidiPair(NoteInstrument.HARP, 0));

		// Guitar
		put(24, new MidiPair(NoteInstrument.GUITAR, 0));
		put(25, new MidiPair(NoteInstrument.GUITAR, 0));
		put(26, new MidiPair(NoteInstrument.GUITAR, 1));
		put(27, new MidiPair(NoteInstrument.GUITAR, 0));
		put(28, new MidiPair(null, 0));
		put(29, new MidiPair(NoteInstrument.GUITAR, -1));
		put(30, new MidiPair(NoteInstrument.GUITAR, -1));
		put(31, new MidiPair(NoteInstrument.GUITAR, 0));

		// Bass
		put(32, new MidiPair(NoteInstrument.BASS, 1));
		put(33, new MidiPair(NoteInstrument.BASS, 2));
		put(34, new MidiPair(NoteInstrument.BASS, 2));
		put(35, new MidiPair(NoteInstrument.BASS, 2));
		put(36, new MidiPair(NoteInstrument.BASS, 2));
		put(37, new MidiPair(NoteInstrument.BASS, 2));
		put(38, new MidiPair(NoteInstrument.BASS, 2));
		put(39, new MidiPair(NoteInstrument.BASS, 2));

		// Strings
		put(40, new MidiPair(NoteInstrument.FLUTE, 0));
		put(41, new MidiPair(NoteInstrument.FLUTE, 0));
		put(42, new MidiPair(NoteInstrument.FLUTE, 0));
		put(43, new MidiPair(NoteInstrument.FLUTE, 0));
		put(44, new MidiPair(NoteInstrument.HARP, 0));
		put(45, new MidiPair(NoteInstrument.HARP, 0));
		put(46, new MidiPair(NoteInstrument.CHIME, 0));
		put(47, new MidiPair(NoteInstrument.SNARE, 1));

		// Ensemble
		put(48, new MidiPair(NoteInstrument.HARP, 0));
		put(49, new MidiPair(NoteInstrument.HARP, 0));
		put(50, new MidiPair(NoteInstrument.HARP, 0));
		put(51, new MidiPair(NoteInstrument.HARP, 0));
		put(52, new MidiPair(NoteInstrument.HARP, 0));
		put(53, new MidiPair(NoteInstrument.HARP, 0));
		put(54, new MidiPair(NoteInstrument.HARP, 0));
		put(55, new MidiPair(NoteInstrument.HARP, 0));

		// Brass
		put(56, new MidiPair(NoteInstrument.TRUMPET, 0));
		put(57, new MidiPair(NoteInstrument.TRUMPET, 0));
		put(58, new MidiPair(NoteInstrument.DIDGERIDOO, 0));
		put(59, new MidiPair(NoteInstrument.TRUMPET_WEATHERED, 0));
		put(60, new MidiPair(NoteInstrument.TRUMPET_EXPOSED, 0));
		put(61, new MidiPair(NoteInstrument.TRUMPET_OXIDIZED, 0));
		put(62, new MidiPair(NoteInstrument.TRUMPET_EXPOSED, 0));
		put(63, new MidiPair(NoteInstrument.TRUMPET_WEATHERED, 0));

		// Reed
		put(64, new MidiPair(NoteInstrument.FLUTE, 0));
		put(65, new MidiPair(NoteInstrument.FLUTE, 0));
		put(66, new MidiPair(NoteInstrument.FLUTE, 0));
		put(67, new MidiPair(NoteInstrument.FLUTE, 0));
		put(68, new MidiPair(NoteInstrument.FLUTE, 0));
		put(69, new MidiPair(NoteInstrument.FLUTE, 0));
		put(70, new MidiPair(NoteInstrument.FLUTE, -1));
		put(71, new MidiPair(NoteInstrument.FLUTE, 0));

		// Pipe
		put(72, new MidiPair(NoteInstrument.FLUTE, -1));
		put(73, new MidiPair(NoteInstrument.FLUTE, -1));
		put(74, new MidiPair(NoteInstrument.FLUTE, -1));
		put(75, new MidiPair(NoteInstrument.FLUTE, -1));
		put(76, new MidiPair(NoteInstrument.FLUTE, -1));
		put(77, new MidiPair(NoteInstrument.FLUTE, -1));
		put(78, new MidiPair(NoteInstrument.FLUTE, -1));
		put(79, new MidiPair(NoteInstrument.FLUTE, -1));

		// Synth lead
		put(80, new MidiPair(NoteInstrument.BIT, 0));
		put(81, new MidiPair(NoteInstrument.BIT, 0));
		put(82, new MidiPair(NoteInstrument.BIT, 0));
		put(83, new MidiPair(NoteInstrument.BIT, 0));
		put(84, new MidiPair(NoteInstrument.BIT, 0));
		put(85, new MidiPair(NoteInstrument.BIT, 0));
		put(86, new MidiPair(NoteInstrument.BIT, 0));
		put(87, new MidiPair(NoteInstrument.BIT, 1));
		put(88, new MidiPair(NoteInstrument.HARP, 0));
		put(89, new MidiPair(NoteInstrument.HARP, 0));
		put(90, new MidiPair(NoteInstrument.HARP, 0));
		put(91, new MidiPair(NoteInstrument.HARP, 0));
		put(92, new MidiPair(NoteInstrument.HARP, 0));
		put(93, new MidiPair(NoteInstrument.HARP, 0));
		put(94, new MidiPair(NoteInstrument.HARP, 0));
		put(95, new MidiPair(NoteInstrument.HARP, 0));

		// Synth effects
		put(96, new MidiPair(null, 0));
		put(97, new MidiPair(null, 0));
		put(98, new MidiPair(NoteInstrument.BIT, 0));
		put(99, new MidiPair(NoteInstrument.HARP, 0));
		put(100, new MidiPair(NoteInstrument.HARP, 0));
		put(101, new MidiPair(null, 0));
		put(102, new MidiPair(null, 0));
		put(103, new MidiPair(null, 0));

		// Ethnic
		put(104, new MidiPair(NoteInstrument.BANJO, 0));
		put(105, new MidiPair(NoteInstrument.BANJO, 0));
		put(106, new MidiPair(NoteInstrument.BANJO, 0));
		put(107, new MidiPair(NoteInstrument.BANJO, 0));
		put(108, new MidiPair(NoteInstrument.BASS, 1));
		put(109, new MidiPair(NoteInstrument.HARP, 0));
		put(110, new MidiPair(NoteInstrument.HARP, 0));
		put(111, new MidiPair(NoteInstrument.HARP, 0));

		// Percussive
		put(112, new MidiPair(NoteInstrument.BELL, -1));
		put(113, new MidiPair(NoteInstrument.HARP, 0));
		put(114, new MidiPair(NoteInstrument.IRON_XYLOPHONE, 0));
		put(115, new MidiPair(NoteInstrument.HAT, 0));
		put(116, new MidiPair(NoteInstrument.SNARE, 0));
		put(117, new MidiPair(NoteInstrument.SNARE, -1));
		put(118, new MidiPair(NoteInstrument.SNARE, 0));

		// Sound effects
		put(119, new MidiPair(null, 0));
		put(120, new MidiPair(null, 0));
		put(121, new MidiPair(null, 0));
		put(122, new MidiPair(null, 0));
		put(123, new MidiPair(null, 0));
		put(124, new MidiPair(null, 0));
		put(125, new MidiPair(null, 0));
		put(126, new MidiPair(null, 0));
		put(127, new MidiPair(NoteInstrument.HARP, 0));
	}};

	private final Map<Integer, MidiPair> MIDI_DRUM = new HashMap<>() {{
		put(24, new MidiPair(null, 0));
		put(25, new MidiPair(null, 0));
		put(26, new MidiPair(null, 0));
		put(27, new MidiPair(null, 0));
		put(28, new MidiPair(null, 0));
		put(29, new MidiPair(null, 0));
		put(30, new MidiPair(null, 0));
		put(31, new MidiPair(null, 0));
		put(32, new MidiPair(null, 0));
		put(33, new MidiPair(null, 0));
		put(34, new MidiPair(null, 0));

		put(35, new MidiPair(NoteInstrument.BASEDRUM, 10));
		put(36, new MidiPair(NoteInstrument.BASEDRUM, 6));
		put(37, new MidiPair(NoteInstrument.HAT, 6));
		put(38, new MidiPair(NoteInstrument.SNARE, 8));
		put(39, new MidiPair(NoteInstrument.HAT, 6));
		put(40, new MidiPair(NoteInstrument.SNARE, 4));
		put(41, new MidiPair(NoteInstrument.BASEDRUM, 6));

		put(42, new MidiPair(NoteInstrument.SNARE, 22));
		put(43, new MidiPair(NoteInstrument.BASEDRUM, 13));
		put(44, new MidiPair(NoteInstrument.SNARE, 22));
		put(45, new MidiPair(NoteInstrument.BASEDRUM, 15));
		put(46, new MidiPair(NoteInstrument.SNARE, 18));
		put(47, new MidiPair(NoteInstrument.BASEDRUM, 20));
		put(48, new MidiPair(NoteInstrument.BASEDRUM, 23));

		put(49, new MidiPair(NoteInstrument.SNARE, 17));
		put(50, new MidiPair(NoteInstrument.BASEDRUM, 23));
		put(51, new MidiPair(NoteInstrument.SNARE, 24));
		put(52, new MidiPair(NoteInstrument.SNARE, 8));
		put(53, new MidiPair(NoteInstrument.SNARE, 13));
		put(54, new MidiPair(NoteInstrument.HAT, 18));
		put(55, new MidiPair(NoteInstrument.SNARE, 18));

		put(56, new MidiPair(NoteInstrument.HAT, 1));
		put(57, new MidiPair(NoteInstrument.SNARE, 13));
		put(58, new MidiPair(NoteInstrument.HAT, 2));
		put(59, new MidiPair(NoteInstrument.SNARE, 13));
		put(60, new MidiPair(NoteInstrument.HAT, 9));
		put(61, new MidiPair(NoteInstrument.HAT, 2));
		put(62, new MidiPair(NoteInstrument.HAT, 8));

		put(63, new MidiPair(NoteInstrument.BASEDRUM, 22));
		put(64, new MidiPair(NoteInstrument.BASEDRUM, 15));
		put(65, new MidiPair(NoteInstrument.SNARE, 13));
		put(66, new MidiPair(NoteInstrument.SNARE, 8));
		put(67, new MidiPair(NoteInstrument.HAT, 8));
		put(68, new MidiPair(NoteInstrument.HAT, 3));
		put(69, new MidiPair(NoteInstrument.HAT, 20));

		put(70, new MidiPair(NoteInstrument.HAT, 23));
		put(71, new MidiPair(null, 0));
		put(72, new MidiPair(null, 0));
		put(73, new MidiPair(NoteInstrument.HAT, 17));
		put(74, new MidiPair(NoteInstrument.HAT, 11));
		put(75, new MidiPair(NoteInstrument.HAT, 18));
		put(76, new MidiPair(NoteInstrument.HAT, 9));

		put(77, new MidiPair(NoteInstrument.HAT, 5));
		put(78, new MidiPair(null, 0));
		put(79, new MidiPair(null, 0));
		put(80, new MidiPair(NoteInstrument.HAT, 17));
		put(81, new MidiPair(NoteInstrument.HAT, 22));
		put(82, new MidiPair(NoteInstrument.SNARE, 22));
		put(83, new MidiPair(null, 0));

		put(84, new MidiPair(null, 0));
		put(85, new MidiPair(NoteInstrument.HAT, 21));
		put(86, new MidiPair(NoteInstrument.BASEDRUM, 14));
		put(87, new MidiPair(NoteInstrument.BASEDRUM, 7));
	}};

	public MidiConverter(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	private record MidiPair(NoteInstrument instrument, int octaveShift) { }

	public boolean convertMidiFile(File midiFile) {
		try {
			Map.Entry<List<String>, List<Integer>> gnbsContent = readMidiFile(midiFile);

			String gnbsFilename = midiFile.getName();
			int extensionPos = gnbsFilename.lastIndexOf(".");
			if(extensionPos != -1) gnbsFilename = gnbsFilename.substring(0, extensionPos);

			File gnbsFile = new File(gMusicMain.getDataFolder(), SongService.GNBS_FOLDER + "/" + gnbsFilename + "." + SongService.GNBS_EXTENSION);
			YamlConfiguration gnbsStruct = YamlConfiguration.loadConfiguration(gnbsFile);

			String title = midiFile.getName().replaceFirst("[.][^.]+$", "");

			gnbsStruct.set("Song.Id", title.replace(" ", ""));
			gnbsStruct.set("Song.Title", title);
			gnbsStruct.set("Song.OriginalAuthor", "");
			gnbsStruct.set("Song.Author", "");
			gnbsStruct.set("Song.Description", new ArrayList<>());
			gnbsStruct.set("Song.Category", "RECORDS");

			for(NoteInstrument inst : NoteInstrument.values()) if(gnbsContent.getValue().contains(inst.getId())) gnbsStruct.set("Song.Content.Instruments." + inst.getId(), inst.getId());

			gnbsStruct.set("Song.Content.Main", gnbsContent.getKey());
			gnbsStruct.save(gnbsFile);

			return true;
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not convert midi file to " + SongService.GNBS_EXTENSION + " file!", e); }

		return false;
	}

	private Map.Entry<List<String>, List<Integer>> readMidiFile(File midiFile) {
		Map<Long, String> tickContent = new TreeMap<>();
		List<String> rows = new ArrayList<>();
		List<Integer> gnbsInstruments = new ArrayList<>();

		try {
			Sequence sequence = MidiSystem.getSequence(midiFile);
			double sequenceMilliLength = (double) (sequence.getMicrosecondLength() / 1000);
			double sequenceTickLength = (double) sequence.getTickLength();

			int[] programByChannel = new int[MIDI_CHANNELS];

			for(Track track : sequence.getTracks()) {
				for(int eventCount = 0; eventCount < track.size(); eventCount++) {
					MidiEvent midiEvent = track.get(eventCount);
					MidiMessage midiMessage = midiEvent.getMessage();
					if(!(midiMessage instanceof ShortMessage shortMidiMessage)) continue;

					int channel = shortMidiMessage.getChannel();
					int command = shortMidiMessage.getCommand();

					if(command == ShortMessage.PROGRAM_CHANGE) {
						programByChannel[channel] = shortMidiMessage.getData1();
						continue;
					}

					if(command != ShortMessage.NOTE_ON) continue;
					if(shortMidiMessage.getData2() <= 0) continue;

					int key = shortMidiMessage.getData1();
					long tick = (long) (((double) midiEvent.getTick() * sequenceMilliLength) / sequenceTickLength);

					MidiPair pair = resolveMidiPair(channel, programByChannel[channel], key);
					if(pair == null || pair.instrument() == null) continue;

					int outputKey;
					if(channel == DRUM_CHANNEL) {
						outputKey = pair.octaveShift();
					} else {
						int fixKey = key - 21;
						outputKey = fixKey - 33;
						if(pair.octaveShift() != 0) outputKey += 12 * pair.octaveShift();
						if(outputKey < 0) outputKey += 12;
					}

					String contentPart = pair.instrument().getId() + "::#" + outputKey;
					String existing = tickContent.get(tick);
					tickContent.put(tick, existing == null ? contentPart : existing + "_" + contentPart);
					if(!gnbsInstruments.contains(pair.instrument().getId())) gnbsInstruments.add(pair.instrument().getId());
				}
			}

			long currentTick = tickContent.keySet().stream().findFirst().orElse(0L);

			for(long rowTick : tickContent.keySet()) {
				rows.add((rowTick - currentTick) + "!" + tickContent.get(rowTick));
				currentTick = rowTick;
			}
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not read midi file!", e); }

		return new AbstractMap.SimpleEntry<>(rows, gnbsInstruments);
	}

	private MidiPair resolveMidiPair(int channel, int program, int key) {
		if(channel == DRUM_CHANNEL) return MIDI_DRUM.get(key);
		return MIDI_INST.get(program);
	}

}