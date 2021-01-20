package com.keuin.kbackupfabric.util.backup.provider;

import com.keuin.kbackupfabric.util.backup.name.BackupFileNameEncoder;

import java.time.LocalDateTime;

public class PrimitiveBackupInformation extends BackupFileNameEncoder.BackupBasicInformation {
    public final long sizeBytes;

    public PrimitiveBackupInformation(String customName, LocalDateTime time, long sizeBytes) {
        super(customName, time);
        this.sizeBytes = sizeBytes;
    }
}
