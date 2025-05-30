package dev.geco.gmusic.object;

public enum GPlayMode {

    DEFAULT(0),
    SHUFFLE(1),
    LOOP(2);

    private final int id;

    GPlayMode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static GPlayMode byId(int id) {
        for(GPlayMode playMode : values()) if(playMode.getId() == id) return playMode;
        return null;
    }

}