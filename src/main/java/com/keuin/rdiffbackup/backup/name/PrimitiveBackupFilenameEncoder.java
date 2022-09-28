package com.keuin.rdiffbackup.backup.name;

import com.keuin.rdiffbackup.util.DateUtil;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrimitiveBackupFilenameEncoder implements BackupFilenameEncoder {
    private static final String BACKUP_FILE_NAME_PREFIX = "backup";

    public static final PrimitiveBackupFilenameEncoder INSTANCE = new PrimitiveBackupFilenameEncoder();

    private PrimitiveBackupFilenameEncoder() {
    }

    @Override
    public String encode(String filename, LocalDateTime time) {
        if (!isValidCustomName(filename))
            throw new IllegalArgumentException("Invalid custom name");
        String timeString = DateUtil.getString(time);
        return BACKUP_FILE_NAME_PREFIX + "-" + timeString + "_" + filename + ".zip";
    }

    @Override
    public BackupBasicInformation decode(String filename) {
        Pattern pattern = Pattern.compile(
                "^" + BACKUP_FILE_NAME_PREFIX + "-" + "([0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2})_(.+)\\.zip" + "$"
        );
        Matcher matcher = pattern.matcher(filename);
        if (matcher.find()) {
            String timeString = matcher.group(1);
            String customName = matcher.group(2);
            return new BackupBasicInformation(customName, DateUtil.toLocalDateTime(timeString));
        }
        return null;
    }
}
