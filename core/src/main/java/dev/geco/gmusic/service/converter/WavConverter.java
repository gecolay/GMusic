package dev.geco.gmusic.service.converter;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.model.NoteInstrument;
import dev.geco.gmusic.service.SongService;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

public class WavConverter {

    private static final float TARGET_SAMPLE_RATE = 44100f;
    private static final int FRAME_SIZE = 2048;
    private static final int HOP_SIZE = 1024;

    private static final double MIN_NOTE_MS = 100.0;
    private static final double MAX_NOTE_GAP_MS = 80.0;
    private static final double MIN_RMS = 0.01;

    private static final double MIN_FREQUENCY = 80.0;
    private static final double MAX_FREQUENCY = 2000.0;

    private static final NoteInstrument DEFAULT_INSTRUMENT = NoteInstrument.HARP;

    private final GMusicMain gMusicMain;

    public WavConverter(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
    }

    public boolean convertWavFile(File wavFile) {
        try {
            List<String> gnbsContent = readWavFile(wavFile);

            String gnbsFilename = wavFile.getName();
            int extensionPos = gnbsFilename.lastIndexOf(".");
            if(extensionPos != -1) gnbsFilename = gnbsFilename.substring(0, extensionPos);

            File gnbsFile = new File(gMusicMain.getDataFolder(), SongService.GNBS_FOLDER + "/" + gnbsFilename + "." + SongService.GNBS_EXTENSION);
            YamlConfiguration gnbsStruct = YamlConfiguration.loadConfiguration(gnbsFile);

            String title = wavFile.getName().replaceFirst("[.][^.]+$", "");

            gnbsStruct.set("Song.Id", title.replace(" ", ""));
            gnbsStruct.set("Song.Title", title);
            gnbsStruct.set("Song.OriginalAuthor", "");
            gnbsStruct.set("Song.Author", "");
            gnbsStruct.set("Song.Description", new ArrayList<>());
            gnbsStruct.set("Song.Category", "RECORDS");

            for(NoteInstrument inst : NoteInstrument.values()) gnbsStruct.set("Song.Content.Instruments." + inst.getId(), inst.getId());

            gnbsStruct.set("Song.Content.Main", gnbsContent);
            gnbsStruct.save(gnbsFile);

            return true;
        } catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not convert wav file to " + SongService.GNBS_EXTENSION + " file!", e); }

        return false;
    }

    private List<String> readWavFile(File wavFile) {
        List<String> rows = new ArrayList<>();
        TreeMap<Long, List<String>> tickContent = new TreeMap<>();

        try(AudioInputStream originalStream = AudioSystem.getAudioInputStream(wavFile)) {
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    TARGET_SAMPLE_RATE,
                    16,
                    1,
                    2,
                    TARGET_SAMPLE_RATE,
                    false
            );

            try(AudioInputStream pcmStream = AudioSystem.getAudioInputStream(targetFormat, originalStream)) {
                double[] samples = readAllSamples(pcmStream);

                List<DetectedNote> notes = detectNotes(samples, TARGET_SAMPLE_RATE);

                for(DetectedNote note : notes) {
                    if(note.durationMs() < MIN_NOTE_MS) continue;

                    long tick = Math.max(0L, Math.round(note.startMs()));
                    String contentPart = DEFAULT_INSTRUMENT.getId() + "::#" + note.gnbsKey();
                    tickContent.computeIfAbsent(tick, k -> new ArrayList<>()).add(contentPart);
                }
            }
        } catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not analyze wav file!", e); }

        long currentTick = tickContent.keySet().stream().findFirst().orElse(0L);
        for(long rowTick : tickContent.keySet()) {
            rows.add((rowTick - currentTick) + "!" + String.join("_", tickContent.get(rowTick)));
            currentTick = rowTick;
        }

        return rows;
    }

    private double[] readAllSamples(AudioInputStream stream) throws Exception {
        byte[] bytes = stream.readAllBytes();
        int sampleCount = bytes.length / 2;
        double[] samples = new double[sampleCount];

        for(int i = 0; i < sampleCount; i++) {
            int low = bytes[i * 2] & 0xFF;
            int high = bytes[i * 2 + 1];
            short sample = (short) ((high << 8) | low);
            samples[i] = sample / 32768.0;
        }

        return samples;
    }

    private List<DetectedNote> detectNotes(double[] samples, float sampleRate) {
        List<DetectedNote> notes = new ArrayList<>();

        Integer currentKey = null;
        double currentStartMs = 0.0;
        double lastFrameTimeMs = 0.0;

        for(int start = 0; start + FRAME_SIZE < samples.length; start += HOP_SIZE) {
            double[] frame = new double[FRAME_SIZE];
            System.arraycopy(samples, start, frame, 0, FRAME_SIZE);

            applyHannWindow(frame);

            double timeMs = (start * 1000.0) / sampleRate;
            double rms = calculateRms(frame);

            Integer detectedKey = null;
            if(rms >= MIN_RMS) {
                double frequency = detectPitchAutocorrelation(frame, sampleRate);
                if(frequency >= MIN_FREQUENCY && frequency <= MAX_FREQUENCY) {
                    int midiNote = frequencyToMidi(frequency);
                    detectedKey = midiToGnbsKey(midiNote);
                }
            }

            if(currentKey == null) {
                if(detectedKey != null) {
                    currentKey = detectedKey;
                    currentStartMs = timeMs;
                }
            } else {
                boolean sameNote = detectedKey != null && detectedKey.equals(currentKey);
                boolean shortGap = detectedKey == null && (timeMs - lastFrameTimeMs) <= MAX_NOTE_GAP_MS;

                if(!sameNote && !shortGap) {
                    notes.add(new DetectedNote(currentStartMs, lastFrameTimeMs, currentKey));
                    currentKey = detectedKey;
                    currentStartMs = detectedKey != null ? timeMs : 0.0;
                }
            }

            lastFrameTimeMs = timeMs;
        }

        if(currentKey != null) notes.add(new DetectedNote(currentStartMs, lastFrameTimeMs, currentKey));

        return mergeShortInstability(notes);
    }

    private List<DetectedNote> mergeShortInstability(List<DetectedNote> notes) {
        if(notes.isEmpty()) return notes;

        List<DetectedNote> merged = new ArrayList<>();
        DetectedNote current = notes.get(0);

        for(int i = 1; i < notes.size(); i++) {
            DetectedNote next = notes.get(i);

            if(next.gnbsKey() == current.gnbsKey()) {
                current = new DetectedNote(current.startMs(), next.endMs(), current.gnbsKey());
                continue;
            }

            if(i + 1 < notes.size()) {
                DetectedNote after = notes.get(i + 1);
                if(next.durationMs() < MIN_NOTE_MS && after.gnbsKey() == current.gnbsKey()) {
                    current = new DetectedNote(current.startMs(), after.endMs(), current.gnbsKey());
                    i++;
                    continue;
                }
            }

            merged.add(current);
            current = next;
        }

        merged.add(current);
        return merged;
    }

    private double calculateRms(double[] frame) {
        double sum = 0.0;
        for(double sample : frame) sum += sample * sample;
        return Math.sqrt(sum / frame.length);
    }

    private void applyHannWindow(double[] frame) {
        for(int i = 0; i < frame.length; i++) {
            frame[i] *= 0.5 * (1.0 - Math.cos((2.0 * Math.PI * i) / (frame.length - 1)));
        }
    }

    private double detectPitchAutocorrelation(double[] frame, float sampleRate) {
        int minLag = (int) (sampleRate / MAX_FREQUENCY);
        int maxLag = (int) (sampleRate / MIN_FREQUENCY);

        double bestCorrelation = Double.NEGATIVE_INFINITY;
        int bestLag = -1;

        for(int lag = minLag; lag <= maxLag; lag++) {
            double correlation = 0.0;
            for(int i = 0; i < frame.length - lag; i++) {
                correlation += frame[i] * frame[i + lag];
            }

            if(correlation > bestCorrelation) {
                bestCorrelation = correlation;
                bestLag = lag;
            }
        }

        if(bestLag <= 0) return -1.0;
        return sampleRate / bestLag;
    }

    private int frequencyToMidi(double frequency) {
        return (int) Math.round(69.0 + 12.0 * (Math.log(frequency / 440.0) / Math.log(2.0)));
    }

    private int midiToGnbsKey(int midiNote) {
        int fixKey = midiNote - 21;
        return fixKey - 33;
    }

    private record DetectedNote(double startMs, double endMs, int gnbsKey) {
        double durationMs() {
            return endMs - startMs;
        }
    }

}