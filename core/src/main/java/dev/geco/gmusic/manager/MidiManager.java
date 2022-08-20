package dev.geco.gmusic.manager;

import java.io.*;
import java.util.*;

import javax.sound.midi.*;

import org.bukkit.configuration.file.*;

import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.values.Values;

public class MidiManager {
	
	private final GMusicMain GPM;
	
	private boolean V2 = true;
	
	public MidiManager(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
	private final Map<Integer, MidiPair> midi_inst = new HashMap<Integer, MidiPair>(); {
		// Piano
		midi_inst.put(0, new MidiPair(0, 0));
		midi_inst.put(1, new MidiPair(0, 0));
		midi_inst.put(2, new MidiPair(13, 0));
		midi_inst.put(3, new MidiPair(0, 0));
		midi_inst.put(4, new MidiPair(13, 0));
		midi_inst.put(5, new MidiPair(13, 0));
		midi_inst.put(6, new MidiPair(0, 1));
		midi_inst.put(7, new MidiPair(0, 0));
		// Chromatic Percussion
		midi_inst.put(8, new MidiPair(11, -1));
		midi_inst.put(9, new MidiPair(11, 0));
		midi_inst.put(10, new MidiPair(11, 0));
		midi_inst.put(11, new MidiPair(11, 0));
		midi_inst.put(12, new MidiPair(11, 0));
		midi_inst.put(13, new MidiPair(9, 0));
		midi_inst.put(14, new MidiPair(7, -1));
		midi_inst.put(15, new MidiPair(7, 0));
		// Organ
		midi_inst.put(16, new MidiPair(1, 1));
		midi_inst.put(17, new MidiPair(1, 1));
		midi_inst.put(18, new MidiPair(0, 0));
		midi_inst.put(19, new MidiPair(0, 0));
		midi_inst.put(20, new MidiPair(0, 0));
		midi_inst.put(21, new MidiPair(0, 0));
		midi_inst.put(22, new MidiPair(0, 0));
		midi_inst.put(23, new MidiPair(0, 0));
		// Guitar
		midi_inst.put(24, new MidiPair(5, 0));
		midi_inst.put(25, new MidiPair(5, 0));
		midi_inst.put(26, new MidiPair(5, 1));
		midi_inst.put(27, new MidiPair(5, 0));
		midi_inst.put(28, new MidiPair(-1, 0));
		midi_inst.put(29, new MidiPair(5, -1));
		midi_inst.put(30, new MidiPair(5, -1));
		midi_inst.put(31, new MidiPair(5, 0));
		// Bass
		midi_inst.put(32, new MidiPair(1, 1));
		midi_inst.put(33, new MidiPair(1, 2));
		midi_inst.put(34, new MidiPair(1, 2));
		midi_inst.put(35, new MidiPair(1, 2));
		midi_inst.put(36, new MidiPair(1, 2));
		midi_inst.put(37, new MidiPair(1, 2));
		midi_inst.put(38, new MidiPair(1, 2));
		midi_inst.put(39, new MidiPair(1, 2));
		// Strings
		midi_inst.put(40, new MidiPair(6, 0));
		midi_inst.put(41, new MidiPair(6, 0));
		midi_inst.put(42, new MidiPair(6, 0));
		midi_inst.put(43, new MidiPair(6, 0));
		midi_inst.put(44, new MidiPair(0, 0));
		midi_inst.put(45, new MidiPair(0, 0));
		midi_inst.put(46, new MidiPair(8, 0));
		midi_inst.put(47, new MidiPair(3, 1));
		// Ensemble
		midi_inst.put(48, new MidiPair(0, 0));
		midi_inst.put(49, new MidiPair(0, 0));
		midi_inst.put(50, new MidiPair(0, 0));
		midi_inst.put(51, new MidiPair(0, 0));
		midi_inst.put(52, new MidiPair(0, 0));
		midi_inst.put(53, new MidiPair(0, 0));
		midi_inst.put(54, new MidiPair(0, 0));
		midi_inst.put(55, new MidiPair(0, 0));
		// Brass
		midi_inst.put(56, new MidiPair(0, 0));
		midi_inst.put(57, new MidiPair(0, 0));
		midi_inst.put(58, new MidiPair(0, 0));
		midi_inst.put(59, new MidiPair(0, 0));
		midi_inst.put(60, new MidiPair(0, 0));
		midi_inst.put(61, new MidiPair(0, 0));
		midi_inst.put(62, new MidiPair(1, 1));
		midi_inst.put(63, new MidiPair(1, 1));
		// Reed
		midi_inst.put(64, new MidiPair(6, 0));
		midi_inst.put(65, new MidiPair(6, 0));
		midi_inst.put(66, new MidiPair(6, 0));
		midi_inst.put(67, new MidiPair(6, 0));
		midi_inst.put(68, new MidiPair(6, 0));
		midi_inst.put(69, new MidiPair(6, 0));
		midi_inst.put(70, new MidiPair(6, -1));
		midi_inst.put(71, new MidiPair(6, 0));
		// Pipe
		midi_inst.put(72, new MidiPair(6, -1));
		midi_inst.put(73, new MidiPair(6, -1));
		midi_inst.put(74, new MidiPair(6, -1));
		midi_inst.put(75, new MidiPair(6, -1));
		midi_inst.put(76, new MidiPair(6, -1));
		midi_inst.put(77, new MidiPair(6, -1));
		midi_inst.put(78, new MidiPair(6, -1));
		midi_inst.put(79, new MidiPair(6, -1));
		// Synth Lead
		midi_inst.put(80, new MidiPair(0, 0));
		midi_inst.put(81, new MidiPair(0, 0));
		midi_inst.put(82, new MidiPair(0, 0));
		midi_inst.put(83, new MidiPair(0, 0));
		midi_inst.put(84, new MidiPair(0, 0));
		midi_inst.put(85, new MidiPair(0, 0));
		midi_inst.put(86, new MidiPair(0, 0));
		midi_inst.put(87, new MidiPair(0, 1));
		midi_inst.put(88, new MidiPair(0, 0));
		midi_inst.put(89, new MidiPair(0, 0));
		midi_inst.put(90, new MidiPair(0, 0));
		midi_inst.put(91, new MidiPair(0, 0));
		midi_inst.put(92, new MidiPair(0, 0));
		midi_inst.put(93, new MidiPair(0, 0));
		midi_inst.put(94, new MidiPair(0, 0));
		midi_inst.put(95, new MidiPair(0, 0));
		// Synth Effects
		midi_inst.put(96, new MidiPair(-1, 0));
		midi_inst.put(97, new MidiPair(-1, 0));
		midi_inst.put(98, new MidiPair(13, 0));
		midi_inst.put(99, new MidiPair(0, 0));
		midi_inst.put(100, new MidiPair(0, 0));
		midi_inst.put(101, new MidiPair(-1, 0));
		midi_inst.put(102, new MidiPair(-1, 0));
		midi_inst.put(103, new MidiPair(-1, 0));
		// Ethnic
		midi_inst.put(104, new MidiPair(14, 0));
		midi_inst.put(105, new MidiPair(14, 0));
		midi_inst.put(106, new MidiPair(14, 0));
		midi_inst.put(107, new MidiPair(14, 0));
		midi_inst.put(108, new MidiPair(1, 1));
		midi_inst.put(109, new MidiPair(0, 0));
		midi_inst.put(110, new MidiPair(0, 0));
		midi_inst.put(111, new MidiPair(0, 0));
		// Percussive
		midi_inst.put(112, new MidiPair(7, -1));
		midi_inst.put(113, new MidiPair(0, 0));
		midi_inst.put(114, new MidiPair(10, 0));
		midi_inst.put(115, new MidiPair(4, 0));
		midi_inst.put(116, new MidiPair(3, 0));
		midi_inst.put(117, new MidiPair(3, -1));
		midi_inst.put(118, new MidiPair(3, 0));
		// Sound Effects
		midi_inst.put(119, new MidiPair(-1, 0));
		midi_inst.put(120, new MidiPair(-1, 0));
		midi_inst.put(121, new MidiPair(-1, 0));
		midi_inst.put(122, new MidiPair(-1, 0));
		midi_inst.put(123, new MidiPair(-1, 0));
		midi_inst.put(124, new MidiPair(-1, 0));
		midi_inst.put(125, new MidiPair(-1, 0));
		midi_inst.put(126, new MidiPair(-1, 0));
		midi_inst.put(127, new MidiPair(0, 0));
	};
	
	private final Map<Integer, MidiPair> midi_drum = new HashMap<Integer, MidiPair>(); {
		// 24
		midi_drum.put(24, new MidiPair(-1, 0));
		midi_drum.put(25, new MidiPair(-1, 0));
		midi_drum.put(26, new MidiPair(-1, 0));
		midi_drum.put(27, new MidiPair(-1, 0));
		midi_drum.put(28, new MidiPair(-1, 0));
		midi_drum.put(29, new MidiPair(-1, 0));
		midi_drum.put(30, new MidiPair(-1, 0));
		midi_drum.put(31, new MidiPair(-1, 0));
		midi_drum.put(32, new MidiPair(-1, 0));
		midi_drum.put(33, new MidiPair(-1, 0));
		midi_drum.put(34, new MidiPair(-1, 0));
		// 35
		midi_drum.put(35, new MidiPair(2, 10));
		midi_drum.put(36, new MidiPair(2, 6));
		midi_drum.put(37, new MidiPair(4, 6));
		midi_drum.put(38, new MidiPair(3, 8));
		midi_drum.put(39, new MidiPair(4, 6));
		midi_drum.put(40, new MidiPair(3, 4));
		midi_drum.put(41, new MidiPair(2, 6));
		// 42
		midi_drum.put(42, new MidiPair(3, 22));
		midi_drum.put(43, new MidiPair(2, 13));
		midi_drum.put(44, new MidiPair(3, 22));
		midi_drum.put(45, new MidiPair(2, 15));
		midi_drum.put(46, new MidiPair(3, 18));
		midi_drum.put(47, new MidiPair(2, 20));
		midi_drum.put(48, new MidiPair(2, 23));
		// 49
		midi_drum.put(49, new MidiPair(3, 17));
		midi_drum.put(50, new MidiPair(2, 23));
		midi_drum.put(51, new MidiPair(3, 24));
		midi_drum.put(52, new MidiPair(3, 8));
		midi_drum.put(53, new MidiPair(3, 13));
		midi_drum.put(54, new MidiPair(4, 18));
		midi_drum.put(55, new MidiPair(3, 18));
		// 56
		midi_drum.put(56, new MidiPair(4, 1));
		midi_drum.put(57, new MidiPair(3, 13));
		midi_drum.put(58, new MidiPair(4, 2));
		midi_drum.put(59, new MidiPair(3, 13));
		midi_drum.put(60, new MidiPair(4, 9));
		midi_drum.put(61, new MidiPair(4, 2));
		midi_drum.put(62, new MidiPair(4, 8));
		// 63
		midi_drum.put(63, new MidiPair(2, 22));
		midi_drum.put(64, new MidiPair(2, 15));
		midi_drum.put(65, new MidiPair(3, 13));
		midi_drum.put(66, new MidiPair(3, 8));
		midi_drum.put(67, new MidiPair(4, 8));
		midi_drum.put(68, new MidiPair(4, 3));
		midi_drum.put(69, new MidiPair(4, 20));
		// 70
		midi_drum.put(70, new MidiPair(4, 23));
		midi_drum.put(71, new MidiPair(-1, 0));
		midi_drum.put(72, new MidiPair(-1, 0));
		midi_drum.put(73, new MidiPair(4, 17));
		midi_drum.put(74, new MidiPair(4, 11));
		midi_drum.put(75, new MidiPair(4, 18));
		midi_drum.put(76, new MidiPair(4, 9));
		// 77
		midi_drum.put(77, new MidiPair(4, 5));
		midi_drum.put(78, new MidiPair(-1, 0));
		midi_drum.put(79, new MidiPair(-1, 0));
		midi_drum.put(80, new MidiPair(4, 17));
		midi_drum.put(81, new MidiPair(4, 22));
		midi_drum.put(82, new MidiPair(3, 22));
		midi_drum.put(83, new MidiPair(-1, 0));
		// 84
		midi_drum.put(84, new MidiPair(-1, 0));
		midi_drum.put(85, new MidiPair(4, 21));
		midi_drum.put(86, new MidiPair(2, 14));
		midi_drum.put(87, new MidiPair(2, 7));
	};
	
	private class MidiPair {
		
		private int pair1;
		
		private int pair2;
		
		public MidiPair(int Pair1, int Pair2) {
			pair1 = Pair1;
			pair2 = Pair2;
		}

		public int getPair1() {
			return pair1;
		}
		
		public int getPair2() {
			return pair2;
		}
	}
	
	public boolean convertFile(File MidiFile) {
		
		File f = MidiFile;
		
		try {
			
			List<String> sc = readMidi(MidiFile);
			
			String f1 = f.getName();
			int pos = f1.lastIndexOf(".");
			if(pos != -1) f1 = f1.substring(0, pos);
			
			File nf = new File("plugins/" + GPM.NAME + "/" + Values.SONGS_PATH + "/" + f1 + Values.GNBS_FILETYP);
			
			try {
				boolean c = nf.createNewFile();
				if(!c) return false;
			} catch(Exception e) { return false; }
			
			YamlConfiguration fc = YamlConfiguration.loadConfiguration(nf);
			
			String t = MidiFile.getName().replaceFirst("[.][^.]+$", "");
			
			fc.set("Song.Id", t.replace(" ", ""));
			fc.set("Song.Title", t);
			fc.set("Song.OAuthor", "");
			fc.set("Song.Author", "");
			fc.set("Song.Description", new ArrayList<>());
			fc.set("Song.Category", "RECORDS");
			
			for(byte i = 0; i < 16; i++) fc.set("Song.Content.Instruments." + i, i);
			
			fc.set("Song.Content.Main", sc);
			
			fc.save(nf);
			
			return true;
			
		} catch (Exception | Error e) { return false; }
		
	}
	
	private List<String> readMidi(File MidiFile) {
		
		Map<Long, String> sc = new TreeMap<Long, String>();
		
		List<String> scr = new ArrayList<>();
		
		try {
			
			Sequence s = MidiSystem.getSequence(MidiFile);
			
			double slm = (double) (s.getMicrosecondLength() / 1000), slt = (double) s.getTickLength();
			
			for(Track t : s.getTracks()) {
				
				int z = 0;
				
				long lt = 0;
				
				int lc = -1;
				
				for(int i = 0; i < t.size(); i++) {
					
					MidiEvent ev = t.get(i);
					
					MidiMessage midim = ev.getMessage();
					
					if(midim instanceof ShortMessage) {
						
						ShortMessage sm = (ShortMessage) midim;
						
						if(sm.getCommand() == ShortMessage.NOTE_ON) {
							
							int key = sm.getData1();
							
							int inst = (int) (!V2 ? midi_inst.get(z).getPair1() : (sm.getChannel() != 9 ? midi_inst.get(z).getPair1() : midi_drum.get(key).getPair1()));
							
							long tick = (long) (((double) ev.getTick() * slm) / slt);
							
							if(inst > -1) {
								
								int fixkey = key - 21;
								int maxfixkey = fixkey - 33;
								
								if(lt != tick || lc != sm.getChannel()) {
									
									if(sm.getChannel() != 9) {
										
										maxfixkey = (int) midi_inst.get(z).getPair2() != 0 ? maxfixkey += (12 * (int) midi_inst.get(z).getPair2()) : maxfixkey;
										
										while(maxfixkey < 0) maxfixkey += 12;
										
										while(maxfixkey > 24) maxfixkey -= 12;
										
										String scm = sc.get(tick);
										
										sc.put(tick, scm == null ? inst + "::#" + maxfixkey : scm + "_" + inst + "::#" + maxfixkey);
										
									} else {
										
										if(!V2) inst = (int) midi_drum.get(key).getPair1();
										
										maxfixkey = (int) midi_drum.get(key).getPair2();
										
										String scm = sc.get(tick);
										
										sc.put(tick, scm == null ? inst + "::#" + maxfixkey : scm + "_" + inst + "::#" + maxfixkey);
										
									}
									
								}
								
							}
							
							lt = tick;
							
							lc = sm.getChannel();
							
						} else if(sm.getCommand() == ShortMessage.PROGRAM_CHANGE) {
							
							z = sm.getData1();
							
						}
						
					}
					
				}
				
			}
			
			long lt = 0;
			
			for(long l : sc.keySet()) {
				
				scr.add((l - lt) + "!" + sc.get(l));
				
				lt = l;
				
			}
			
		} catch(Exception | Error e) { }
		
		return scr;
		
	}
	
}