package dev.geco.gmusic.cmd.tab;

import java.util.*;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.object.*;

public class GMusicTabComplete implements TabCompleter {

    private final GMusicMain GPM;

    public GMusicTabComplete(GMusicMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {

        List<String> complete = new ArrayList<>(), completeStarted = new ArrayList<>();

        if(Sender instanceof Player) {

            if(Args.length == 1) {

                if(GPM.getPManager().hasPermission(Sender, "Music")) {

                    complete.add("play");
                    complete.add("playing");
                    complete.add("random");
                    complete.add("stop");
                    complete.add("pause");
                    complete.add("resume");
                    complete.add("skip");
                    complete.add("toggle");
                }

                if(!Args[Args.length - 1].isEmpty()) {

                    for(String entry : complete) if(entry.toLowerCase().startsWith(Args[Args.length - 1].toLowerCase())) completeStarted.add(entry);

                    complete.clear();
                }
            } else if(Args.length == 2) {

                if(GPM.getPManager().hasPermission(Sender, "Music")) {

                    if(Args[0].equalsIgnoreCase("play")) {
                        for (GSong song : GPM.getSongManager().getSongs()) complete.add(song.getId());
                    }
                }

                if(!Args[Args.length - 1].isEmpty()) {

                    for(String entry : complete) if(entry.toLowerCase().startsWith(Args[Args.length - 1].toLowerCase())) completeStarted.add(entry);

                    complete.clear();
                }
            }
        }

        return complete.isEmpty() ? completeStarted : complete;
    }

}