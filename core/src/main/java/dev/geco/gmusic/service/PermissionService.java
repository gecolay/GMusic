package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PermissionService {

    public boolean hasPermission(Permissible permissible, String... permissions) {
        if(!(permissible instanceof Player)) return true;
        for(String permission : permissions) {
            if(permissible.isPermissionSet(GMusicMain.NAME + "." + permission)) return permissible.hasPermission(GMusicMain.NAME + "." + permission);
            if(permissible.hasPermission(GMusicMain.NAME + "." + permission)) return true;
        }
        return permissible.hasPermission(GMusicMain.NAME + ".*");
    }

}