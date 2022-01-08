package me.rosenbach.s3backup.enums;

public enum Configuration {

    ACCESS_KEY_ID("access-key-id"),
    ACCESS_KEY_SECRET("access-key-secret"),
    BUCKET("bucket"),
    REGION("region"),
    BACKUP_INTERVAL("backup-interval");

    private String key;

    Configuration(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
