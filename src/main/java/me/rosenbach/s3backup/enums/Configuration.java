package me.rosenbach.s3backup.enums;

public enum Configuration {

    POSTFIX("postfix"),
    ACCESS_KEY_ID("access-key-id"),
    ACCESS_KEY_SECRET("access-key-secret"),
    BUCKET("bucket"),
    REGION("region"),
    BACKUP_TIMES("backup-times"),
    UPLOAD_SPEED("upload-speed");

    private final String key;

    Configuration(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
