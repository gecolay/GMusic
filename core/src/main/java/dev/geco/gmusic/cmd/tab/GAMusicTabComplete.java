package dev.geco.gmusic.cmd.tab;

import java.util.*;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gmusic.GMusicMain;

public class GAMusicTabComplete implements TabCompleter {

    private final GMusicMain GPM;

    public GAMusicTabComplete(GMusicMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {

        List<String> complete = new ArrayList<>(), completeStarted = new ArrayList<>();

        if(Sender instanceof Player) {

            if(Args.length == 1) {

                if(GPM.getPManager().hasPermission(Sender, "AMusic")) {}

                if(!Args[Args.length - 1].isEmpty()) {

                    for(String entry : complete) if(entry.toLowerCase().startsWith(Args[Args.length - 1].toLowerCase())) completeStarted.add(entry);

                    complete.clear();
                }
            }
        }

        return complete.isEmpty() ? completeStarted : complete;
    }

}