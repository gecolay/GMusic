package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

public class MidiService {

	private final GMusicMain gMusicMain;
	private final boolean version2 = true;
	private final Map<Integer, MidiPair> MIDI_INST = new HashMap<>(); {
		// Piano
		MIDI_INST.put(0, new MidiPair(0, 0));
		MIDI_INST.put(1, new MidiPair(0, 0));
		MIDI_INST.put(2, new MidiPair(13, 0));
		MIDI_INST.put(3, new MidiPair(0, 0));
		MIDI_INST.put(4, new MidiPair(13, 0));
		MIDI_INST.put(5, new MidiPair(13, 0));
		MIDI_INST.put(6, new MidiPair(0, 1));
		MIDI_INST.put(7, new MidiPair(0, 0));
		// Chromatic percussion
		MIDI_INST.put(8, new MidiPair(11, -1));
		MIDI_INST.put(9, new MidiPair(11, 0));
		MIDI_INST.put(10, new MidiPair(11, 0));
		MIDI_INST.put(11, new MidiPair(11, 0));
		MIDI_INST.put(12, new MidiPair(11, 0));
		MIDI_INST.put(13, new MidiPair(9, 0));
		MIDI_INST.put(14, new MidiPair(7, -1));
		MIDI_INST.put(15, new MidiPair(7, 0));
		// Organ
		MIDI_INST.put(16, new MidiPair(1, 1));
		MIDI_INST.put(17, new MidiPair(1, 1));
		MIDI_INST.put(18, new MidiPair(0, 0));
		MIDI_INST.put(19, new MidiPair(0, 0));
		MIDI_INST.put(20, new MidiPair(0, 0));
		MIDI_INST.put(21, new MidiPair(0, 0));
		MIDI_INST.put(22, new MidiPair(0, 0));
		MIDI_INST.put(23, new MidiPair(0, 0));
		// Guitar
		MIDI_INST.put(24, new MidiPair(5, 0));
		MIDI_INST.put(25, new MidiPair(5, 0));
		MIDI_INST.put(26, new MidiPair(5, 1));
		MIDI_INST.put(27, new MidiPair(5, 0));
		MIDI_INST.put(28, new MidiPair(-1, 0));
		MIDI_INST.put(29, new MidiPair(5, -1));
		MIDI_INST.put(30, new MidiPair(5, -1));
		MIDI_INST.put(31, new MidiPair(5, 0));
		// Bass
		MIDI_INST.put(32, new MidiPair(1, 1));
		MIDI_INST.put(33, new MidiPair(1, 2));
		MIDI_INST.put(34, new MidiPair(1, 2));
		MIDI_INST.put(35, new MidiPair(1, 2));
		MIDI_INST.put(36, new MidiPair(1, 2));
		MIDI_INST.put(37, new MidiPair(1, 2));
		MIDI_INST.put(38, new MidiPair(1, 2));
		MIDI_INST.put(39, new MidiPair(1, 2));
		// Strings
		MIDI_INST.put(40, new MidiPair(6, 0));
		MIDI_INST.put(41, new MidiPair(6, 0));
		MIDI_INST.put(42, new MidiPair(6, 0));
		MIDI_INST.put(43, new MidiPair(6, 0));
		MIDI_INST.put(44, new MidiPair(0, 0));
		MIDI_INST.put(45, new MidiPair(0, 0));
		MIDI_INST.put(46, new MidiPair(8, 0));
		MIDI_INST.put(47, new MidiPair(3, 1));
		// Ensemble
		MIDI_INST.put(48, new MidiPair(0, 0));
		MIDI_INST.put(49, new MidiPair(0, 0));
		MIDI_INST.put(50, new MidiPair(0, 0));
		MIDI_INST.put(51, new MidiPair(0, 0));
		MIDI_INST.put(52, new MidiPair(0, 0));
		MIDI_INST.put(53, new MidiPair(0, 0));
		MIDI_INST.put(54, new MidiPair(0, 0));
		MIDI_INST.put(55, new MidiPair(0, 0));
		// Brass
		MIDI_INST.put(56, new MidiPair(0, 0));
		MIDI_INST.put(57, new MidiPair(0, 0));
		MIDI_INST.put(58, new MidiPair(0, 0));
		MIDI_INST.put(59, new MidiPair(0, 0));
		MIDI_INST.put(60, new MidiPair(0, 0));
		MIDI_INST.put(61, new MidiPair(0, 0));
		MIDI_INST.put(62, new MidiPair(1, 1));
		MIDI_INST.put(63, new MidiPair(1, 1));
		// Reed
		MIDI_INST.put(64, new MidiPair(6, 0));
		MIDI_INST.put(65, new MidiPair(6, 0));
		MIDI_INST.put(66, new MidiPair(6, 0));
		MIDI_INST.put(67, new MidiPair(6, 0));
		MIDI_INST.put(68, new MidiPair(6, 0));
		MIDI_INST.put(69, new MidiPair(6, 0));
		MIDI_INST.put(70, new MidiPair(6, -1));
		MIDI_INST.put(71, new MidiPair(6, 0));
		// Pipe
		MIDI_INST.put(72, new MidiPair(6, -1));
		MIDI_INST.put(73, new MidiPair(6, -1));
		MIDI_INST.put(74, new MidiPair(6, -1));
		MIDI_INST.put(75, new MidiPair(6, -1));
		MIDI_INST.put(76, new MidiPair(6, -1));
		MIDI_INST.put(77, new MidiPair(6, -1));
		MIDI_INST.put(78, new MidiPair(6, -1));
		MIDI_INST.put(79, new MidiPair(6, -1));
		// Synth lead
		MIDI_INST.put(80, new MidiPair(0, 0));
		MIDI_INST.put(81, new MidiPair(0, 0));
		MIDI_INST.put(82, new MidiPair(0, 0));
		MIDI_INST.put(83, new MidiPair(0, 0));
		MIDI_INST.put(84, new MidiPair(0, 0));
		MIDI_INST.put(85, new MidiPair(0, 0));
		MIDI_INST.put(86, new MidiPair(0, 0));
		MIDI_INST.put(87, new MidiPair(0, 1));
		MIDI_INST.put(88, new MidiPair(0, 0));
		MIDI_INST.put(89, new MidiPair(0, 0));
		MIDI_INST.put(90, new MidiPair(0, 0));
		MIDI_INST.put(91, new MidiPair(0, 0));
		MIDI_INST.put(92, new MidiPair(0, 0));
		MIDI_INST.put(93, new MidiPair(0, 0));
		MIDI_INST.put(94, new MidiPair(0, 0));
		MIDI_INST.put(95, new MidiPair(0, 0));
		// Synth effects
		MIDI_INST.put(96, new MidiPair(-1, 0));
		MIDI_INST.put(97, new MidiPair(-1, 0));
		MIDI_INST.put(98, new MidiPair(13, 0));
		MIDI_INST.put(99, new MidiPair(0, 0));
		MIDI_INST.put(100, new MidiPair(0, 0));
		MIDI_INST.put(101, new MidiPair(-1, 0));
		MIDI_INST.put(102, new MidiPair(-1, 0));
		MIDI_INST.put(103, new MidiPair(-1, 0));
		// Ethnic
		MIDI_INST.put(104, new MidiPair(14, 0));
		MIDI_INST.put(105, new MidiPair(14, 0));
		MIDI_INST.put(106, new MidiPair(14, 0));
		MIDI_INST.put(107, new MidiPair(14, 0));
		MIDI_INST.put(108, new MidiPair(1, 1));
		MIDI_INST.put(109, new MidiPair(0, 0));
		MIDI_INST.put(110, new MidiPair(0, 0));
		MIDI_INST.put(111, new MidiPair(0, 0));
		// Percussive
		MIDI_INST.put(112, new MidiPair(7, -1));
		MIDI_INST.put(113, new MidiPair(0, 0));
		MIDI_INST.put(114, new MidiPair(10, 0));
		MIDI_INST.put(115, new MidiPair(4, 0));
		MIDI_INST.put(116, new MidiPair(3, 0));
		MIDI_INST.put(117, new MidiPair(3, -1));
		MIDI_INST.put(118, new MidiPair(3, 0));
		// Sound effects
		MIDI_INST.put(119, new MidiPair(-1, 0));
		MIDI_INST.put(120, new MidiPair(-1, 0));
		MIDI_INST.put(121, new MidiPair(-1, 0));
		MIDI_INST.put(122, new MidiPair(-1, 0));
		MIDI_INST.put(123, new MidiPair(-1, 0));
		MIDI_INST.put(124, new MidiPair(-1, 0));
		MIDI_INST.put(125, new MidiPair(-1, 0));
		MIDI_INST.put(126, new MidiPair(-1, 0));
		MIDI_INST.put(127, new MidiPair(0, 0));
	}
	private final Map<Integer, MidiPair> MIDI_DRUM = new HashMap<>(); {
		// 24
		MIDI_DRUM.put(24, new MidiPair(-1, 0));
		MIDI_DRUM.put(25, new MidiPair(-1, 0));
		MIDI_DRUM.put(26, new MidiPair(-1, 0));
		MIDI_DRUM.put(27, new MidiPair(-1, 0));
		MIDI_DRUM.put(28, new MidiPair(-1, 0));
		MIDI_DRUM.put(29, new MidiPair(-1, 0));
		MIDI_DRUM.put(30, new MidiPair(-1, 0));
		MIDI_DRUM.put(31, new MidiPair(-1, 0));
		MIDI_DRUM.put(32, new MidiPair(-1, 0));
		MIDI_DRUM.put(33, new MidiPair(-1, 0));
		MIDI_DRUM.put(34, new MidiPair(-1, 0));
		// 35
		MIDI_DRUM.put(35, new MidiPair(2, 10));
		MIDI_DRUM.put(36, new MidiPair(2, 6));
		MIDI_DRUM.put(37, new MidiPair(4, 6));
		MIDI_DRUM.put(38, new MidiPair(3, 8));
		MIDI_DRUM.put(39, new MidiPair(4, 6));
		MIDI_DRUM.put(40, new MidiPair(3, 4));
		MIDI_DRUM.put(41, new MidiPair(2, 6));
		// 42
		MIDI_DRUM.put(42, new MidiPair(3, 22));
		MIDI_DRUM.put(43, new MidiPair(2, 13));
		MIDI_DRUM.put(44, new MidiPair(3, 22));
		MIDI_DRUM.put(45, new MidiPair(2, 15));
		MIDI_DRUM.put(46, new MidiPair(3, 18));
		MIDI_DRUM.put(47, new MidiPair(2, 20));
		MIDI_DRUM.put(48, new MidiPair(2, 23));
		// 49
		MIDI_DRUM.put(49, new MidiPair(3, 17));
		MIDI_DRUM.put(50, new MidiPair(2, 23));
		MIDI_DRUM.put(51, new MidiPair(3, 24));
		MIDI_DRUM.put(52, new MidiPair(3, 8));
		MIDI_DRUM.put(53, new MidiPair(3, 13));
		MIDI_DRUM.put(54, new MidiPair(4, 18));
		MIDI_DRUM.put(55, new MidiPair(3, 18));
		// 56
		MIDI_DRUM.put(56, new MidiPair(4, 1));
		MIDI_DRUM.put(57, new MidiPair(3, 13));
		MIDI_DRUM.put(58, new MidiPair(4, 2));
		MIDI_DRUM.put(59, new MidiPair(3, 13));
		MIDI_DRUM.put(60, new MidiPair(4, 9));
		MIDI_DRUM.put(61, new MidiPair(4, 2));
		MIDI_DRUM.put(62, new MidiPair(4, 8));
		// 63
		MIDI_DRUM.put(63, new MidiPair(2, 22));
		MIDI_DRUM.put(64, new MidiPair(2, 15));
		MIDI_DRUM.put(65, new MidiPair(3, 13));
		MIDI_DRUM.put(66, new MidiPair(3, 8));
		MIDI_DRUM.put(67, new MidiPair(4, 8));
		MIDI_DRUM.put(68, new MidiPair(4, 3));
		MIDI_DRUM.put(69, new MidiPair(4, 20));
		// 70
		MIDI_DRUM.put(70, new MidiPair(4, 23));
		MIDI_DRUM.put(71, new MidiPair(-1, 0));
		MIDI_DRUM.put(72, new MidiPair(-1, 0));
		MIDI_DRUM.put(73, new MidiPair(4, 17));
		MIDI_DRUM.put(74, new MidiPair(4, 11));
		MIDI_DRUM.put(75, new MidiPair(4, 18));
		MIDI_DRUM.put(76, new MidiPair(4, 9));
		// 77
		MIDI_DRUM.put(77, new MidiPair(4, 5));
		MIDI_DRUM.put(78, new MidiPair(-1, 0));
		MIDI_DRUM.put(79, new MidiPair(-1, 0));
		MIDI_DRUM.put(80, new MidiPair(4, 17));
		MIDI_DRUM.put(81, new MidiPair(4, 22));
		MIDI_DRUM.put(82, new MidiPair(3, 22));
		MIDI_DRUM.put(83, new MidiPair(-1, 0));
		// 84
		MIDI_DRUM.put(84, new MidiPair(-1, 0));
		MIDI_DRUM.put(85, new MidiPair(4, 21));
		MIDI_DRUM.put(86, new MidiPair(2, 14));
		MIDI_DRUM.put(87, new MidiPair(2, 7));
	}

	public MidiService(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	private record MidiPair(int first, int second) { }

	public boolean convertMidiFile(File midiFile) {
		try {
			List<String> gnbsContent = readMidiFile(midiFile);

			String gnbsFilename = midiFile.getName();
			int extensionPos = gnbsFilename.lastIndexOf(".");
			if(extensionPos != -1) gnbsFilename = gnbsFilename.substring(0, extensionPos);

			File gnbsFile = new File(gMusicMain.getDataFolder(), "songs/" + gnbsFilename + ".gnbs");
			YamlConfiguration gnbsStruct = YamlConfiguration.loadConfiguration(gnbsFile);

			String title = midiFile.getName().replaceFirst("[.][^.]+$", "");

			gnbsStruct.set("Song.Id", title.replace(" ", ""));
			gnbsStruct.set("Song.Title", title);
			gnbsStruct.set("Song.OAuthor", "");
			gnbsStruct.set("Song.Author", "");
			gnbsStruct.set("Song.Description", new ArrayList<>());
			gnbsStruct.set("Song.Category", "RECORDS");

			for(byte instrument = 0; instrument < 16; instrument++) gnbsStruct.set("Song.Content.Instruments." + instrument, instrument);

			gnbsStruct.set("Song.Content.Main", gnbsContent);

			gnbsStruct.save(gnbsFile);

			return true;
		} catch (Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not convert midi file to gnbs file!", e); }

		return false;
	}

	private List<String> readMidiFile(File midiFile) {
		Map<Long, String> tickContent = new TreeMap<>();
		List<String> rows = new ArrayList<>();

		try {
			Sequence sequence = MidiSystem.getSequence(midiFile);

			double sequenceMilliLength = (double) (sequence.getMicrosecondLength() / 1000), sequenceTickLength = (double) sequence.getTickLength();

			for(Track track : sequence.getTracks()) {
				int lastData = 0;

				for(int eventCount = 0; eventCount < track.size(); eventCount++) {
					MidiEvent midiEvent = track.get(eventCount);
					MidiMessage midiMessage = midiEvent.getMessage();
					if(!(midiMessage instanceof ShortMessage shortMidiMessage)) continue;

                    if(shortMidiMessage.getCommand() == ShortMessage.PROGRAM_CHANGE) {
						lastData = shortMidiMessage.getData1();
						continue;
					}

					if(shortMidiMessage.getCommand() != ShortMessage.NOTE_ON) continue;

					int key = shortMidiMessage.getData1();
					int instrument = (!version2 ? MIDI_INST.get(lastData).first() : (shortMidiMessage.getChannel() != 9 ? MIDI_INST.get(lastData).first() : MIDI_DRUM.get(key).first()));
					long tick = (long) (((double) midiEvent.getTick() * sequenceMilliLength) / sequenceTickLength);

					if(instrument > -1) {
						int fixkey = key - 21;
						int maxfixkey = fixkey - 33;

						if(shortMidiMessage.getChannel() != 9) {
							maxfixkey = MIDI_INST.get(lastData).second() != 0 ? maxfixkey + (12 * MIDI_INST.get(lastData).second()) : maxfixkey;
							if(maxfixkey < 0) maxfixkey += 12;
							String scm = tickContent.get(tick);
							tickContent.put(tick, scm == null ? instrument + "::#" + maxfixkey : scm + "_" + instrument + "::#" + maxfixkey);
						} else {
							if(!version2) instrument = MIDI_DRUM.get(key).first();
							maxfixkey = MIDI_DRUM.get(key).second();
							String scm = tickContent.get(tick);
							tickContent.put(tick, scm == null ? instrument + "::#" + maxfixkey : scm + "_" + instrument + "::#" + maxfixkey);
						}
					}
				}
			}

			long currentTick = tickContent.keySet().stream().findFirst().orElse(0L);

			for(long rowTick : tickContent.keySet()) {
				rows.add((rowTick - currentTick) + "!" + tickContent.get(rowTick));
				currentTick = rowTick;
			}
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not read midi file!", e); }

		return rows;
	}

}