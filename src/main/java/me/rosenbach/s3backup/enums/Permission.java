package me.rosenbach.s3backup.enums;

public enum Permission {

    BACKUP("me.rosenbach.s3backup.backup");

    private String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String value() {
        return this.permission;
    }
}
