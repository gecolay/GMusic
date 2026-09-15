package dev.geco.gmusic.model;

import dev.geco.gmusic.GMusicMain;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CustomInstrument implements Instrument {

    private final String sound;
    private final boolean isKnownSoundEvent;
    private final int key;

    public CustomInstrument(String sound, int key) {
        Optional<String> soundEvent = GMusicMain.getInstance().getSoundEventService().getSoundEvent(sound);
        this.sound = soundEvent.orElse(sound);
        this.isKnownSoundEvent = soundEvent.isPresent();
        this.key = key;
    }

    @Override
    public @NotNull String getSound() { return sound; }

    public boolean isKnownSoundEvent() { return isKnownSoundEvent; }

    @Override
    public int getInstrumentKey() { return key; }

    @Override
    public boolean isCustom() { return true; }

}