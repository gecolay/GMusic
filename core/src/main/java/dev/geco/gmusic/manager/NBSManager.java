package dev.geco.gmusic.manager;

import java.io.*;
import java.util.*;

import org.bukkit.configuration.file.*;

import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.values.*;

public class NBSManager {
	
	private final GMusicMain GPM;
	
    public NBSManager(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
	public boolean convertFile(File NBSFile) {
		
		File f = NBSFile;
		
		try {
			
			DataInputStream dis = new DataInputStream(new FileInputStream(f));
			
			short l = readShort(dis);
			int v = 0;
			
			if(l == 0) {
				v = dis.readByte();
				dis.readByte();
				if(v >= 3) l = readShort(dis);
			}
			
			short h = readShort(dis);
			String t = readString(dis);
			if(t.equals("")) t = NBSFile.getName().replaceFirst("[.][^.]+$", "");
			String a = readString(dis);
			String o = readString(dis);
			String d = readString(dis);
			float s = readShort(dis) / 100f;
			dis.readBoolean();
			dis.readByte();
			dis.readByte();
			readInt(dis);
			readInt(dis);
			readInt(dis);
			readInt(dis);
			readInt(dis);
			readString(dis);
			
			if(v >= 4) {
				dis.readByte();
				dis.readByte();
				readShort(dis);
			}
			
			List<String> sc = new ArrayList<>();
			List<Byte> il = new ArrayList<>();
			
			while(true) {
				
				short jt = readShort(dis);
				if(jt == 0) break;
				
				String c = ((long) ((sc.size() == 0 ? jt - 1 : jt) * 1000 / s)) + "!";
				
				while(true) {
					
					short jl = readShort(dis);
					if(jl == 0) break;
					byte i = dis.readByte();
					byte k = dis.readByte();
					int p = 100;
					
					if(v >= 4) {
						dis.readByte();
						p = 200 - dis.readUnsignedByte();
						readShort(dis);
					}
					
					String c1 = i + "::#" + (k - 33) + (p == 100 ? "" : ":" + p);
					
					c += c.endsWith("!") ? c1 : "_" + c1;
					
					if(!il.contains(i)) il.add(i);
					
				}
				
				if(sc.size() > 0) {
					
					String[] l1 = sc.get(sc.size() - 1).split(";");
					
					if(c.equals(l1[0])) {
						
						sc.remove(sc.size() - 1);
						
						sc.add(c + ";" + ((l1.length == 1 || l1[1].equals("") ? 0 : Long.parseLong(l1[1])) + 1));
						
					} else sc.add(c);
					
				} else sc.add(c);
				
			}
			
			for(int i = 0; i < h; i++) {
				readString(dis);
				if(v >= 4) dis.readByte();
				dis.readByte();
				if(v >= 2) dis.readByte();
			}
			
			byte ca = dis.readByte();
			
			List<String> ro = new ArrayList<>();
			
			for(int i = 0; i < ca; i++) {
				readString(dis);
				ro.add(readString(dis).replace(".ogg", ""));
				dis.readByte();
				dis.readByte();
			}
			
			String f1 = f.getName();
    		int pos = f1.lastIndexOf(".");
    		if(pos != -1) f1 = f1.substring(0, pos);
			
    		File nf = new File("plugins/" + GPM.NAME + "/" + Values.SONGS_PATH + "/" + f1 + Values.GNBS_FILETYP);
    		
			try {
				boolean c = nf.createNewFile();
				if(!c) return false;
			} catch(Exception e) { return false; }
			
			YamlConfiguration fc = YamlConfiguration.loadConfiguration(nf);
			
			fc.set("Song.Id", t.replace(" ", ""));
			fc.set("Song.Title", t);
			fc.set("Song.OAuthor", o);
			fc.set("Song.Author", a);
			fc.set("Song.Description", d.replace(" ", "").equals("") ? new ArrayList<>() : Arrays.asList(d.split("\n")));
			fc.set("Song.Category", "RECORDS");
			
			for(byte i = 0; i < 16; i++) if(il.contains(i)) fc.set("Song.Content.Instruments." + i, i);
			
			for(int i = 16; i < 16 + ro.size(); i++) fc.set("Song.Content.Instruments." + i, ro.get(i - 16));
			
			fc.set("Song.Content.Main", sc);
			
			fc.save(nf);
			
			return true;
			
		} catch (Exception | Error e) { return false; }
		
	}
	
	private short readShort(DataInputStream DIS) throws IOException {
		int i1 = DIS.readUnsignedByte();
		int i2 = DIS.readUnsignedByte();
		return (short) (i1 + (i2 << 8));
	}
	
	private int readInt(DataInputStream DIS) throws IOException {
		int i1 = DIS.readUnsignedByte();
		int i2 = DIS.readUnsignedByte();
		int i3 = DIS.readUnsignedByte();
		int i4 = DIS.readUnsignedByte();
		return (i1 + (i2 << 8) + (i3 << 16) + (i4 << 24));
	}
	
	private String readString(DataInputStream DIS) throws IOException {
		int l = readInt(DIS);
		StringBuilder sb = new StringBuilder(l);
		for(; l > 0; --l) {
			char c = (char) DIS.readByte();
			sb.append(c == (char) 0x0D ? ' ' : c);
		}
		return sb.toString();
	}
	
}