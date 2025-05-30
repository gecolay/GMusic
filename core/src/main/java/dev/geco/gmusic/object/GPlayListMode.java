package dev.geco.gmusic.object;

public enum GPlayListMode {

    DEFAULT(0),
    FAVORITES(1),
    RADIO(2);

    private final int id;

    GPlayListMode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static GPlayListMode byId(int id) {
        for(GPlayListMode playListMode : values()) if(playListMode.getId() == id) return playListMode;
        return null;
    }

}