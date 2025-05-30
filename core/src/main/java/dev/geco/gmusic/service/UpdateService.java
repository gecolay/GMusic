package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;

public class UpdateService {

    private final String REMOTE_URL = "https://api.spigotmc.org/legacy/update.php?resource=";
    private final GMusicMain gMusicMain;
    private LocalDate lastCheckDate = null;
    private String latestVersion = null;
    private boolean isLatestVersion = true;

    public UpdateService(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
    }

    public void checkForUpdates() {
        if(!gMusicMain.getConfigService().CHECK_FOR_UPDATE) return;
        checkVersion();
        if(isLatestVersion) return;
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(!gMusicMain.getPermissionService().hasPermission(player, "Update")) continue;
            gMusicMain.getMessageService().sendMessage(player, "Plugin.plugin-update", "%Name%", GMusicMain.NAME, "%NewVersion%", latestVersion, "%Version%", gMusicMain.getDescription().getVersion(), "%Path%", gMusicMain.getDescription().getWebsite());
        }
        gMusicMain.getMessageService().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-update", "%Name%", GMusicMain.NAME, "%NewVersion%", latestVersion, "%Version%", gMusicMain.getDescription().getVersion(), "%Path%", gMusicMain.getDescription().getWebsite());
    }

    public void checkForUpdates(Player player) {
        if(!gMusicMain.getConfigService().CHECK_FOR_UPDATE) return;
        if(!gMusicMain.getPermissionService().hasPermission(player, "Update")) return;
        checkVersion();
        if(isLatestVersion) return;
        gMusicMain.getMessageService().sendMessage(player, "Plugin.plugin-update", "%Name%", GMusicMain.NAME, "%NewVersion%", latestVersion, "%Version%", gMusicMain.getDescription().getVersion(), "%Path%", gMusicMain.getDescription().getWebsite());
    }

    private void getSpigotVersion(Consumer<String> versionConsumer) {
        gMusicMain.getTaskService().run(() -> {
            try(InputStream inputStream = new URL(REMOTE_URL + GMusicMain.RESOURCE_ID).openStream();
                Scanner scanner = new Scanner(inputStream)) {
                if(scanner.hasNext() && versionConsumer != null) versionConsumer.accept(scanner.next());
            } catch(IOException e) {
                if(e.getMessage().contains("50")) return;
                gMusicMain.getLogger().log(Level.SEVERE, "Could not get remote version!", e);
            }
        }, false);
    }

    private void checkVersion() {
        LocalDate today = LocalDate.now();
        if(lastCheckDate != null && lastCheckDate.equals(today)) return;
        lastCheckDate = today;
        if(GMusicMain.RESOURCE_ID.charAt(0) == '0') {
            isLatestVersion = true;
            return;
        }
        try {
            getSpigotVersion(spigotVersion -> {
                latestVersion = spigotVersion;
                if(latestVersion == null) {
                    isLatestVersion = true;
                    return;
                }
                String pluginVersion = gMusicMain.getDescription().getVersion();
                String[] pluginVersionParts = getShortVersion(pluginVersion).split("\\.");
                String[] spigotVersionParts = getShortVersion(latestVersion).split("\\.");
                int minLength = Math.min(pluginVersionParts.length, spigotVersionParts.length);
                for(int i = 0; i < minLength; i++) {
                    int pluginPart = Integer.parseInt(pluginVersionParts[i]);
                    int spigotPart = Integer.parseInt(spigotVersionParts[i]);
                    if(pluginPart < spigotPart) {
                        isLatestVersion = false;
                        return;
                    } else if(pluginPart > spigotPart) {
                        isLatestVersion = true;
                        return;
                    }
                }
                isLatestVersion = pluginVersionParts.length >= spigotVersionParts.length;
            });
        } catch(Throwable e) { isLatestVersion = true; }
    }

    private String getShortVersion(String version) { return version.replaceAll("[\\[\\] ]", ""); }

}