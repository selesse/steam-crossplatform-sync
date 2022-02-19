package com.selesse.steam.appcache;

public class App {
    private final int appId;
    private final int size;
    private final int infoState;
    private final int lastUpdated;
    private final int picsToken;
    private final byte[] sha1;
    private final int changeNumber;
    private final VdfObject vdfObject;

    public App(int appId, int size, int infoState, int lastUpdated, int picsToken, byte[] sha1, int changeNumber, VdfObject vdfObject) {
        this.appId = appId;
        this.size = size;
        this.infoState = infoState;
        this.lastUpdated = lastUpdated;
        this.picsToken = picsToken;
        this.sha1 = sha1;
        this.changeNumber = changeNumber;
        this.vdfObject = vdfObject;
    }

    public int getAppId() {
        return appId;
    }

    public int getSize() {
        return size;
    }

    public int getInfoState() {
        return infoState;
    }

    public int getLastUpdated() {
        return lastUpdated;
    }

    public int getPicsToken() {
        return picsToken;
    }

    public byte[] getSha1() {
        return sha1;
    }

    public int getChangeNumber() {
        return changeNumber;
    }

    public VdfObject getVdfObject() {
        return vdfObject;
    }
}
