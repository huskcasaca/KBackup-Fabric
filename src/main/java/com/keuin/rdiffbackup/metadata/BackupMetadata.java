package com.keuin.rdiffbackup.metadata;

import java.io.Serial;
import java.io.Serializable;

/**
 * WARNING: DO NOT modify this class, or the plugin will be incompatible with backups created by older versions.
 */
public class BackupMetadata implements Serializable {
    public static final String METADATA_FILENAME = "backup_metadata";

    @Serial
    private static final long serialVersionUID = 1L;
    private final long time;
    private final String name;

    public BackupMetadata(long time, String name) {
        this.time = time;
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public String getName() {
        return name;
    }
}
