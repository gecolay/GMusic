package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class VersionService {

    private final String LATEST_VERSION = "v26_1";
    private final HashMap<String, String> VERSION_MAPPING = new HashMap<>(); {
        VERSION_MAPPING.put("v1_18_1", "v1_18");
        VERSION_MAPPING.put("v1_19_2", "v1_19_1");
        VERSION_MAPPING.put("v1_20_1", "v1_20");
        VERSION_MAPPING.put("v1_20_4", "v1_20_3");
        VERSION_MAPPING.put("v1_20_6", "v1_20_5");
        VERSION_MAPPING.put("v1_21_1", "v1_21");
        VERSION_MAPPING.put("v1_21_3", "v1_21_2");
        VERSION_MAPPING.put("v1_21_7", "v1_21_6");
        VERSION_MAPPING.put("v1_21_8", "v1_21_6");
        VERSION_MAPPING.put("v1_21_10", "v1_21_9");
        VERSION_MAPPING.put("v26_1_1", "v26_1");
    }
    private final GMusicMain gMusicMain;
    private final String serverVersion;
    private final int[] serverVersionParts;
    private String packagePath;
    private boolean available;

    public VersionService(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
        serverVersion = extractServerVersion();
        serverVersionParts = Arrays.stream(serverVersion.split("\\.")).mapToInt(Integer::parseInt).toArray();
        if(!isNewerOrVersion(1, 18)) return;
        String packageVersion = "v" + serverVersion.replace(".", "_");
        packagePath = gMusicMain.getClass().getPackage().getName() + ".mcv." + VERSION_MAPPING.getOrDefault(packageVersion, packageVersion);
        available = hasPackageClass("model.gui.MusicInputGUI");
        if(available) return;
        packagePath = gMusicMain.getClass().getPackage().getName() + ".mcv." + LATEST_VERSION;
        available = hasPackageClass("model.gui.MusicInputGUI");
    }

    private String extractServerVersion() {
        String rawServerVersion = Bukkit.getServer().getVersion();
        int mcIndexStart = rawServerVersion.indexOf("MC:");
        if(mcIndexStart != -1) {
            mcIndexStart += 4;
            int mcIndexEnd = rawServerVersion.indexOf(')', mcIndexStart);
            if(mcIndexEnd != -1) rawServerVersion = rawServerVersion.substring(mcIndexStart, mcIndexEnd);
        }
        return rawServerVersion.split(" ")[0].trim();
    }

    public String getServerVersion() { return serverVersion; }

    public String getPackagePath() { return packagePath; }

    public boolean isAvailable() { return available; }

    public boolean isNewerOrVersion(int... version) {
        int max = Math.max(serverVersionParts.length, version.length);
        for(int i = 0; i < max; i++) {
            int sv = (i < serverVersionParts.length) ? serverVersionParts[i] : 0;
            int tv = (i < version.length) ? version[i] : 0;
            if (sv > tv) return true;
            if (sv < tv) return false;
        }
        return true;
    }

    public Object executeMethod(Object object, String methodName) {
        try {
            Method method = object.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(object);
        } catch(Throwable ignored) { }
        return null;
    }

    public boolean hasPackageClass(String className) {
        try {
            Class.forName(packagePath + "." + className);
            return true;
        } catch(Throwable ignored) { }
        return false;
    }

    public String getPackageVersion() {
        return packagePath.substring(packagePath.lastIndexOf('.') + 1);
    }

}