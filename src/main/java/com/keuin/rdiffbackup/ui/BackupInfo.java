package com.keuin.rdiffbackup.ui;

import com.keuin.rdiffbackup.operation.backup.method.ConfiguredBackupMethod;
import com.keuin.rdiffbackup.util.DateUtil;
import net.minecraft.server.MinecraftServer;

import java.time.LocalDateTime;

/**
 * Used in UI part. Holds necessary information for displaying a backup.
 */
public interface BackupInfo {
    String getName();

    LocalDateTime getCreationTime();

    long getSizeBytes();

    String getType();

    default String getCanonicalName() {
        return getName() + "-" + DateUtil.getString(getCreationTime());
    }

    ConfiguredBackupMethod createConfiguredBackupMethod(MinecraftServer server);

    /**
     * This is depreciated. But eliminating all usages needs to refactor the UI code.
     *
     * @return the backup file name.
     */
    @Deprecated
    String getBackupFileName();
}