package dev.geco.gmusic.util;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.model.NotePart;
import dev.geco.gmusic.model.PlaySettings;
import dev.geco.gmusic.model.Song;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MusicUtil {

    private static final double SIMULATED_RANGE_CAP = 8.0;

    private final GMusicMain gMusicMain;

    public MusicUtil(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
    }

    public void playAtPlayer(@NotNull Player player, @NotNull NotePart notePart, @NotNull PlaySettings playSettings) {
        play(player, notePart, null, playSettings.getFixedVolume(), playSettings.isStereo());
    }
    public void playAtLocation(@NotNull Player player, @NotNull NotePart notePart, @NotNull Location origin, @NotNull PlaySettings playSettings) {
        if(gMusicMain.getConfigService().J_LOCATIONAL_CLOSE_TO_PLAYER) {
            Location playAt = moveTowardsOrigin(player.getEyeLocation(), origin);
            play(player, notePart, playAt, playSettings.getFixedVolume(), false);
        } else {
            float volume = rangeToVolume(playSettings.getRange()) * playSettings.getFixedVolume();
            play(player, notePart, origin, volume, false);
        }
    }
    public void playAtPlayerWithDecay(@NotNull Player player, @NotNull NotePart notePart, double distanceToOrigin, @NotNull PlaySettings playSettings) {
        float volume = simulateVolumeDecay(distanceToOrigin, playSettings.getRange()) * playSettings.getFixedVolume();
        play(player, notePart, null, volume, playSettings.isStereo());
    }

    private void play(@NotNull Player player, @NotNull NotePart notePart, @Nullable Location origin, float fixedVolume, boolean stereo) {
        Song song = notePart.getNote().getSong();
        if(notePart.getSound() != null) {
            Sound sound = getSound(player, notePart, fixedVolume);
            if(stereo) {
                Location originLocation = origin == null ? player.getEyeLocation() : origin;
                Location stereoLocation = notePart.getDistance() == 0 ? originLocation : gMusicMain.getSteroNoteUtil().convertToStero(originLocation, notePart.getDistance());
                player.playSound(stereoLocation, notePart.getSound(), song.getSoundCategory(), sound.volume(), sound.pitch());
            } else {
                if(origin == null) player.playSound(sound, Sound.Emitter.self());
                else player.playSound(origin, notePart.getSound(), song.getSoundCategory(), sound.volume(), sound.pitch());
            }
        } else if(notePart.getStopSound() != null) player.stopSound(notePart.getStopSound(), song.getSoundCategory());
    }

    private Sound getSound(@NotNull Player player, @NotNull NotePart notePart, float fixedVolume) {
        Key sound = Key.key(notePart.getSound());
        SoundCategory category = notePart.getNote().getSong().getSoundCategory();
        float volume = fixedVolume * notePart.getVolume();
        if(gMusicMain.getConfigService().ENVIRONMENT_EFFECTS && gMusicMain.getEnvironmentUtil().isPlayerSwimming(player))
            return Sound.sound(sound, category, volume > 0.4f ? volume - 0.3f : volume, notePart.getPitch() - 0.15f);
        else
            return Sound.sound(sound, category, volume, notePart.getPitch());
    }

    private Location moveTowardsOrigin(Location listener, Location origin) {
        Vector listenerToOrigin = origin.toVector().subtract(listener.toVector());
        double lengthSqr = listenerToOrigin.lengthSquared();
        if(lengthSqr <= SIMULATED_RANGE_CAP * SIMULATED_RANGE_CAP) return origin;
        return listener.clone().add(listenerToOrigin.normalize().multiply(SIMULATED_RANGE_CAP));
    }
    private float rangeToVolume(double range) {
        return (float) (1.0 + (range - 16) * 0.06);
    }
    private float simulateVolumeDecay(double playerDistance, double range) {
        return (float) ((range - playerDistance) / range);
    }

}
