package dev.geco.gmusic.cmd;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.objects.*;

public class GMusicCommand implements CommandExecutor {

    private final GMusicMain GPM;

    public GMusicCommand(GMusicMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public boolean onCommand(@NotNull CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {

        if(!(Sender instanceof Player)) {

            GPM.getMManager().sendMessage(Sender, "Messages.command-sender-error");
            return true;
        }

        Player player = (Player) Sender;

        if(!GPM.getPManager().hasPermission(Sender, "Music")) {

            GPM.getMManager().sendMessage(Sender, "Messages.command-permission-error");
            return true;
        }

        if(Args.length == 0) {

            GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-use-error");
            return true;
        }

        Song song;

        switch (Args[0].toLowerCase()) {
            case "play":
                if(Args.length == 1) {
                    GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-use-error");
                    return true;
                }
                song = GPM.getSongManager().getSongById(Args[1]);
                if(song == null) {
                    GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-song-error", "%Song%", Args[1]);
                    return true;
                }
                GPM.getPlaySongManager().playSong(player, song);
                GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-play", "%Song%", song.getId(), "%SongTitle%", song.getTitle());
                break;
            case "playing":
                if(!GPM.getPlaySongManager().hasPlayingSong(player.getUniqueId()) || GPM.getPlaySongManager().hasPausedSong(player.getUniqueId())) {
                    GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-playing-error");
                    return true;
                }
                song = GPM.getPlaySongManager().getPlayingSong(player.getUniqueId());
                GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-playing", "%Song%", song.getId(), "%SongTitle%", song.getTitle());
                break;
            case "random":
                song = GPM.getPlaySongManager().getRandomSong(player.getUniqueId());
                if(song == null) {
                    GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-no-song-error");
                    return true;
                }
                GPM.getPlaySongManager().playSong(player, song);
                GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-play", "%Song%", song.getId(), "%SongTitle%", song.getTitle());
                break;
            case "stop":
                if(!GPM.getPlaySongManager().hasPlayingSong(player.getUniqueId())) {
                    GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-playing-error");
                    return true;
                }
                GPM.getPlaySongManager().stopSong(player);
                GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-stop");
                break;
            case "pause":
                if(!GPM.getPlaySongManager().hasPlayingSong(player.getUniqueId()) || GPM.getPlaySongManager().hasPausedSong(player.getUniqueId())) {
                    GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-playing-error");
                    return true;
                }
                GPM.getPlaySongManager().pauseSong(player);
                GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-pause");
                break;
            case "resume":
                if(!GPM.getPlaySongManager().hasPausedSong(player.getUniqueId())) {
                    GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-paused-error");
                    return true;
                }
                GPM.getPlaySongManager().resumeSong(player);
                GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-resume");
                break;
            case "skip":
                song = GPM.getPlaySongManager().getNextSong(player);
                if(song == null) {
                    GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-no-song-error");
                    return true;
                }
                GPM.getPlaySongManager().playSong(player, song);
                GPM.getMManager().sendMessage(Sender, "Messages.command-gmusic-play", "%Song%", song.getId(), "%SongTitle%", song.getTitle());
                break;
            case "toggle":
                PlaySettings playSettings = GPM.getPlaySettingsManager().getPlaySettings(player.getUniqueId());
                playSettings.setToggleMode(!playSettings.isToggleMode());
                break;
        }

        return true;
    }

}