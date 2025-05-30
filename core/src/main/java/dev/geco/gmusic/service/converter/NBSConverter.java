package dev.geco.gmusic.service.converter;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class NBSConverter {

	private final GMusicMain gMusicMain;

	public NBSConverter(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	public void convertNBSFile(File nbsFile) {
		try {
			DataInputStream dataInput = new DataInputStream(Files.newInputStream(nbsFile.toPath()));

			short type = readShort(dataInput);
			int version = 0;

			if(type == 0) {
				version = dataInput.readByte();
				dataInput.readByte();
				if(version >= 3) readShort(dataInput);
			}

			short layerCount = readShort(dataInput);
			String title = readString(dataInput);
			if(title.isEmpty()) title = nbsFile.getName().replaceFirst("[.][^.]+$", "");
			String author = readString(dataInput);
			String originalAuthor = readString(dataInput);
			String description = readString(dataInput);
			float sequence = readShort(dataInput) / 100f;
			dataInput.readBoolean();
			dataInput.readByte();
			dataInput.readByte();
			readInt(dataInput);
			readInt(dataInput);
			readInt(dataInput);
			readInt(dataInput);
			readInt(dataInput);
			readString(dataInput);

			if(version >= 4) {
				dataInput.readByte();
				dataInput.readByte();
				readShort(dataInput);
			}

			List<String> gnbsContent = new ArrayList<>();
			List<Byte> gnbsInstruments = new ArrayList<>();

			// Get the volume and direction of each layer in the song
			List<Byte> layerVolumes = new ArrayList<>();
			List<Integer> layerDirections = new ArrayList<>();
			readLayerInfo(nbsFile, layerCount, layerVolumes, layerDirections);

			int currentLayer = -1;

			while(true) {

				short jt = readShort(dataInput);
				if(jt == 0) break;

				StringBuilder content = new StringBuilder(((long) ((gnbsContent.isEmpty() ? jt - 1 : jt) * 1000 / sequence)) + "!");

				while(true) {

					short jl = readShort(dataInput);
					if(jl == 0) {
						currentLayer = -1;
						break;
					}
					currentLayer = currentLayer + jl;
					byte i = dataInput.readByte();
					byte k = dataInput.readByte();
					int p = 100;
					int v = 100;

					if(version >= 4) {
						v = dataInput.readByte();
						p = 200 - dataInput.readUnsignedByte();
						readShort(dataInput);
					}

					// Combine the layer volume with the noteblock volume
					// If the layer panning is not center, combine the layer & noteblock direction
					v = (layerVolumes.get(currentLayer) * v) / 100;
					if (layerDirections.get(currentLayer) != 100) {
						p = (layerDirections.get(currentLayer) + p) / 2;
					}

					String contentPart = i + ":" + v + ":#" + (k - 33) + (p == 100 ? "" : ":" + p);

					content.append(content.toString().endsWith("!") ? contentPart : "_" + contentPart);

					if(!gnbsInstruments.contains(i)) gnbsInstruments.add(i);
				}

				// minify gnbsContent
				if(!gnbsContent.isEmpty()) {
					String[] tick = gnbsContent.get(gnbsContent.size() - 1).split(";");
					if(content.toString().equals(tick[0])) {
						gnbsContent.remove(gnbsContent.size() - 1);
						gnbsContent.add(content + ";" + ((tick.length == 1 || tick[1].isEmpty() ? 0 : Long.parseLong(tick[1])) + 1));
					} else gnbsContent.add(content.toString());
				} else gnbsContent.add(content.toString());
			}

			for(int layer = 0; layer < layerCount; layer++) {
				readString(dataInput);
				if(version >= 4) dataInput.readByte();
				dataInput.readByte();
				if(version >= 2) dataInput.readByte();
			}

			byte midiInstrumentsLength = dataInput.readByte();

			List<String> midiInstruments = new ArrayList<>();

			for(int instrumentCount = 0; instrumentCount < midiInstrumentsLength; instrumentCount++) {
				readString(dataInput);
				midiInstruments.add(readString(dataInput).replace(".ogg", ""));
				dataInput.readByte();
				dataInput.readByte();
			}

			String gnbsFilename = nbsFile.getName();
			int extensionPos = gnbsFilename.lastIndexOf(".");
			if(extensionPos != -1) gnbsFilename = gnbsFilename.substring(0, extensionPos);

			File gnbsFile = new File(gMusicMain.getDataFolder(), "songs/" + gnbsFilename + ".gnbs");

			YamlConfiguration gnbsStruct = YamlConfiguration.loadConfiguration(gnbsFile);

			gnbsStruct.set("Song.Id", title.replace(" ", ""));
			gnbsStruct.set("Song.Title", title);
			gnbsStruct.set("Song.OAuthor", originalAuthor);
			gnbsStruct.set("Song.Author", author);
			gnbsStruct.set("Song.Description", description.replace(" ", "").isEmpty() ? new ArrayList<>() : Arrays.asList(description.split("\n")));
			gnbsStruct.set("Song.Category", "RECORDS");

			for(byte instrument = 0; instrument < 16; instrument++) if(gnbsInstruments.contains(instrument)) gnbsStruct.set("Song.Content.Instruments." + instrument, instrument);

			for(int instrument = 16; instrument < 16 + midiInstruments.size(); instrument++) gnbsStruct.set("Song.Content.Instruments." + instrument, midiInstruments.get(instrument - 16));

			gnbsStruct.set("Song.Content.Main", gnbsContent);

			gnbsStruct.save(gnbsFile);
		} catch (Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not convert nbs file to gnbs file!", e); }
	}

	private short readShort(DataInputStream dataInput) throws IOException {
		int i1 = dataInput.readUnsignedByte();
		int i2 = dataInput.readUnsignedByte();
		return (short) (i1 + (i2 << 8));
	}

	private int readInt(DataInputStream dataInput) throws IOException {
		int i1 = dataInput.readUnsignedByte();
		int i2 = dataInput.readUnsignedByte();
		int i3 = dataInput.readUnsignedByte();
		int i4 = dataInput.readUnsignedByte();
		return (i1 + (i2 << 8) + (i3 << 16) + (i4 << 24));
	}

	private String readString(DataInputStream dataInput) throws IOException {
		int length = readInt(dataInput);
		StringBuilder builder = new StringBuilder(length);
		for(; length > 0; --length) {
			char c = (char) dataInput.readByte();
			builder.append(c == (char) 0x0D ? ' ' : c);
		}
		return builder.toString();
	}

	private void readLayerInfo(File nbsFile, Short layerCount, List<Byte> layerVolumes, List<Integer> layerDirections) {
		try {
			DataInputStream dataInput = new DataInputStream(Files.newInputStream(nbsFile.toPath()));

			// Skip through header section, we don't care about this
			short type = readShort(dataInput);
			int version = 0;

			if(type == 0) {
				version = dataInput.readByte();
				dataInput.readByte();
				if(version >= 3) readShort(dataInput);
			}

			readShort(dataInput);
			readString(dataInput);
			readString(dataInput);
			readString(dataInput);
			readString(dataInput);
			readShort(dataInput);
			dataInput.readBoolean();
			dataInput.readByte();
			dataInput.readByte();
			readInt(dataInput);
			readInt(dataInput);
			readInt(dataInput);
			readInt(dataInput);
			readInt(dataInput);
			readString(dataInput);

			if(version >= 4) {
				dataInput.readByte();
				dataInput.readByte();
				readShort(dataInput);
			}

			// Skip through note blocks section, we don't care about this either
			while(true) {

				short jt = readShort(dataInput);
				if(jt == 0) break;

				while(true) {

					short jl = readShort(dataInput);
					if(jl == 0) break;
					dataInput.readByte();
					dataInput.readByte();

					if(version >= 4) {
						dataInput.readByte();
						dataInput.readUnsignedByte();
						readShort(dataInput);
					}
				}
			}

			// Get volume and direction of each layer
			// This is the bit we actually care about
			for(int layer = 0; layer < layerCount; layer++) {
				readString(dataInput);
				if(version >= 4) dataInput.readByte();

				byte layerVolume = dataInput.readByte();
				layerVolumes.add(layerVolume);

				int layerDirection = 100;
				if(version >= 2) {
					layerDirection = 200 - dataInput.readUnsignedByte();
				}
				layerDirections.add(layerDirection);
			}
		} catch (Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not read nbs layer!", e); }
	}

}