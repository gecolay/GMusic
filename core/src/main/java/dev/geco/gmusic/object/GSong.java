package dev.geco.gmusic.object;

import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GSong {

	private static final List<Material> DISCS = Arrays.stream(Material.values()).filter(disc -> disc.name().contains("_DISC_")).toList();
	private final String filename;
	private final String id;
	private final String title;
	private final String originalAuthor;
	private final String author;
	private final List<String> description;
	private Material discMaterial;
	private SoundCategory soundCategory;
	private final HashMap<String, String> instruments = new HashMap<>();
	private final HashMap<String, List<GNote>> parts = new HashMap<>();
	private final List<GNote> notes = new ArrayList<>();
	private final HashMap<Long, List<GNotePart>> content = new HashMap<>();
	private long noteAmount = 0;
	private long length = 0;

    public GSong(File gnbsFile) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(gnbsFile);
		filename = gnbsFile.getName();

		id = config.getString("Song.Id");
		title = config.getString("Song.Title", id);
		originalAuthor = config.getString("Song.OAuthor");
		author = config.getString("Song.Author");
		description = config.getStringList("Song.Description");
		String material = config.getString("Song.Material");
		if(material != null) {
			try { discMaterial = Material.valueOf(material.toUpperCase()); } catch(IllegalArgumentException ignored) { }
		}
		if(discMaterial == null) discMaterial = id == null ? DISCS.get(0) : DISCS.get(id.length() <= DISCS.size() - 1 ? id.length() : id.length() % (DISCS.size() - 1));
		try { soundCategory = SoundCategory.valueOf(config.getString("Song.Category", "").toUpperCase()); } catch(IllegalArgumentException e) { soundCategory = SoundCategory.RECORDS; }

		List<String> songInstruments = new ArrayList<>();
		try { songInstruments.addAll(config.getConfigurationSection("Song.Content.Instruments").getKeys(false)); } catch (Throwable ignored) { }
		for(String songInstrument : songInstruments) {
			try {
				String noteInstrument = GNoteInstrument.getInstrument(Integer.parseInt(config.getString("Song.Content.Instruments." + songInstrument, "0")));
				if(noteInstrument != null) instruments.put(songInstrument, noteInstrument);
				else throw new IllegalArgumentException();
			} catch(IllegalArgumentException e) { instruments.put(songInstrument, config.getString("Song.Content.Instruments." + songInstrument)); }
		}

		List<String> songParts = new ArrayList<>();
		try { songParts.addAll(config.getConfigurationSection("Song.Content.Parts").getKeys(false)); } catch (Throwable ignored) { }

		for(String songPart : songParts) {
			List<GNote> songNotes = new ArrayList<>();
			for(String contentPart : config.getStringList("Song.Content.Parts." + songPart)) songNotes.add(new GNote(this, contentPart));
			parts.put(songPart, songNotes);
		}

		List<String> mainContent = config.getStringList("Song.Content.Main");
		for(String row : mainContent) notes.add(new GNote(this, row));

		for(GNote note : notes) {
			if(note.isReference()) {
				for(long noteCount = 1; noteCount <= note.getAmount(); noteCount++) {
					length += note.getDelay();

					for(GNote noteReference : note.getReferences()) {
						for(long nodeReferenceCount = 1; nodeReferenceCount <= noteReference.getAmount(); nodeReferenceCount++) {
							length += noteReference.getDelay();

							if(content.containsKey(length)) {
								List<GNotePart> noteParts = content.get(length);
								noteParts.addAll(noteReference.getNoteParts());
								content.put(length, noteParts);
							} else content.put(length, noteReference.getNoteParts());

							noteAmount += noteReference.getNoteParts().size();
						}
					}
				}
				continue;
			}

			for(long noteCount = 1; noteCount <= note.getAmount(); noteCount++) {
				length += note.getDelay();

				if(content.containsKey(length)) {
					List<GNotePart> noteParts = content.get(length);
					noteParts.addAll(note.getNoteParts());
					content.put(length, noteParts);
				} else content.put(length, note.getNoteParts());

				noteAmount += note.getNoteParts().size();
			}
		}
	}

	public String getFileName() { return filename; }

	public String getId() { return id; }

	public String getTitle() { return title; }

	public String getOriginalAuthor() { return originalAuthor; }

	public String getAuthor() { return author; }

	public List<String> getDescription() { return description; }

	public Material getDiscMaterial() { return discMaterial; }

	public SoundCategory getSoundCategory() { return soundCategory; }

	public HashMap<String, String> getInstruments() { return instruments; }

	public HashMap<String, List<GNote>> getParts() { return parts; }

	public List<GNote> getMain() { return notes; }

	public HashMap<Long, List<GNotePart>> getContent() { return content; }

	public long getStepAmount() { return content.size(); }

	public long getNoteAmount() { return noteAmount; }

	public long getLength() { return length; }

}