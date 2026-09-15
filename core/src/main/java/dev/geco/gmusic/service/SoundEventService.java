package dev.geco.gmusic.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dev.geco.gmusic.GMusicMain;
import io.papermc.paper.ServerBuildInfo;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class SoundEventService {

    private static final Gson GSON = initGson();
    private static final URL VERSION_MANIFEST_URL = url("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
    private static final URI ASSET_URL_BASE = uri("https://resources.download.minecraft.net");

    private final GMusicMain gMusicMain;
    private @Nullable Map<String, String> soundNameToEvent = null;

    public SoundEventService(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
    }

    public void loadSoundEvents() {
        if(gMusicMain.getConfigService().DOWNLOAD_SOUND_EVENTS) {
            Path cachePath = gMusicMain.getDataPath().resolve("sound_cache.json");
            SoundEventCache cached = readCache(cachePath);

            String currentVersion = ServerBuildInfo.buildInfo().minecraftVersionId();
            try {
                if(cached != null && currentVersion.equals(cached.version))
                    soundNameToEvent = cached.soundMap;
                else {
                    soundNameToEvent = fetchSoundMapping(currentVersion);
                    SoundEventCache toCache = new SoundEventCache(currentVersion, soundNameToEvent);
                    writeCache(cachePath, toCache);
                }
            } catch(Throwable e) {
                gMusicMain.getLogger().log(Level.WARNING, "Fetching sound events failed", e);
                if(cached != null) soundNameToEvent = cached.soundMap;
            }
        }
        if(soundNameToEvent == null) gMusicMain.getLogger().warning("Sound events are not loaded, some custom instruments may not work");
    }

    private @Nullable SoundEventService.SoundEventCache readCache(Path path) {
        if(!Files.exists(path)) return null;
        try {
            return Files.exists(path) ? read(path, SoundEventCache.class) : null;
        } catch(Throwable e) {
            gMusicMain.getLogger().log(Level.WARNING, "Loading sound event cache failed", e);
            return null;
        }
    }
    private void writeCache(Path path, SoundEventCache soundEventCache) {
        try {
            write(path, soundEventCache);
        } catch(Throwable e) {
            gMusicMain.getLogger().log(Level.WARNING, "Saving sound event cache failed", e);
        }
    }

    private record SoundEventCache(
            String version,
            Map<String, String> soundMap
    ) {}

    private Map<String, String> fetchSoundMapping(String version) throws IOException {
        gMusicMain.getLogger().info("Downloading sound events...");

        VersionManifest versionManifest = fetch(VERSION_MANIFEST_URL, VersionManifest.class);
        URL clientJsonUrl = versionManifest.versions.stream()
                .filter(v -> version.equals(v.id))
                .findAny()
                .map(v -> v.url)
                .orElseThrow();

        ClientJson clientJson = fetch(clientJsonUrl, ClientJson.class);
        URL assetsUrl = clientJson.assetIndex.url;

        Assets assets = fetch(assetsUrl, Assets.class);
        String hash = assets.objects.get("minecraft/sounds.json").hash;
        URL soundsJsonUrl = ASSET_URL_BASE.resolve(hash.substring(0, 2) + "/" + hash).toURL();

        Map<String, SoundEvent> soundsJson = fetch(soundsJsonUrl, SOUNDS_JSON_TYPE);
        Map<String, String> soundNameToEvent = new HashMap<>();
        for(Map.Entry<String, SoundEvent> entry : soundsJson.entrySet()) {
            String eventId = entry.getKey();
            SoundEvent soundEvent = entry.getValue();
            for(Sound sound : soundEvent.sounds) soundNameToEvent.put(sound.name, eventId);
        }
        gMusicMain.getLogger().info("Sound events downloaded");
        return soundNameToEvent;
    }

    private static <T> T fetch(URL source, Class<T> clazz) throws IOException {
        return GSON.fromJson(new InputStreamReader(source.openStream()), clazz);
    }
    private static <T> T fetch(URL source, TypeToken<T> typeToken) throws IOException {
        return GSON.fromJson(new InputStreamReader(source.openStream()), typeToken);
    }
    private static <T> T read(Path path, Class<T> clazz) throws IOException {
        return GSON.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), clazz);
    }
    private static <T> void write(Path path, T obj) throws IOException {
        try(Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(obj, writer);
        }
    }

    private record VersionManifest(
            List<Version> versions
    ) {}
    private record Version(
            String id,
            URL url
    ) {}

    private record ClientJson(
            AssetIndex assetIndex
    ) {}
    private record AssetIndex(
            URL url
    ) {}

    private record Assets(
            Map<String, Asset> objects
    ) {}
    private record Asset(
            String hash
    ) {}

    private static final TypeToken<Map<String, SoundEvent>> SOUNDS_JSON_TYPE = new TypeToken<>(){};
    private record SoundEvent(
            List<Sound> sounds
    ) {}
    private record Sound(
            String name
    ) {}

    // A sound can either be:
    private static class SoundDeserializer implements JsonDeserializer<Sound> {
        @Override
        public Sound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            // An object: {"name": "minecraft:sound_id"}
            if(json.isJsonObject()) return new Sound(json.getAsJsonObject().get("name").getAsString());
            // A string: "minecraft:sound_id"
            return new Sound(json.getAsString());
        }
    }

    private static Gson initGson() {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(Sound.class, new SoundDeserializer());
        return gson.create();
    }

    private static URL url(String url) {
        try {
            return new URL(url);
        } catch(MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    private static URI uri(String uri) {
        try {
            return new URI(uri);
        } catch(URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    public boolean areSoundEventsLoaded() {
        return soundNameToEvent != null;
    }

    public Optional<String> getSoundEvent(String soundPath) {
        String baseSound = soundPath.startsWith("minecraft/") ? soundPath.substring("minecraft/".length()) : soundPath;
        return soundNameToEvent == null ? Optional.empty() : Optional.ofNullable(soundNameToEvent.get(baseSound));
    }

}
