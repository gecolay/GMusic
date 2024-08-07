package dev.geco.gmusic.manager;

import java.lang.reflect.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.inventory.*;

import dev.geco.gmusic.GMusicMain;

public class SVManager {

    private final String SERVER_VERSION;

    protected final HashMap<String, String> VERSION_MAPPING = new HashMap<>(); {

        VERSION_MAPPING.put("v1_18_1", "v1_18");
        VERSION_MAPPING.put("v1_19_2", "v1_19_1");
        VERSION_MAPPING.put("v1_20_1", "v1_20");
        VERSION_MAPPING.put("v1_20_4", "v1_20_3");
        VERSION_MAPPING.put("v1_20_6", "v1_20_5");
    }

    public SVManager(GMusicMain GPluginMain) {
        String version = Bukkit.getServer().getBukkitVersion();
        SERVER_VERSION = version.substring(0, version.indexOf('-'));
    }

    public String getServerVersion() { return SERVER_VERSION; }

    public boolean isNewerOrVersion(int Version, int SubVersion) {
        String[] version = SERVER_VERSION.split("\\.");
        return Integer.parseInt(version[1]) > Version || (Integer.parseInt(version[1]) == Version && (version.length > 2 ? Integer.parseInt(version[2]) >= SubVersion : SubVersion == 0));
    }

    public Inventory getInventoryFromView(Object View, String InventoryMethod) {
        try {
            Method method = View.getClass().getMethod(InventoryMethod);
            method.setAccessible(true);
            return (Inventory) method.invoke(View);
        } catch (Throwable ignored) { }
        return null;
    }

}