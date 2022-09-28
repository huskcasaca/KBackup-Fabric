package com.keuin.rdiffbackup.backup.name;

import com.keuin.rdiffbackup.util.DateUtil;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IncrementalBackupFilenameEncoder implements BackupFilenameEncoder {

    private static final String BACKUP_FILENAME_PREFIX = "incremental";
    public static final IncrementalBackupFilenameEncoder INSTANCE = new IncrementalBackupFilenameEncoder();

    private IncrementalBackupFilenameEncoder() {
    }

    @Override
    public String encode(String filename, LocalDateTime time) {
        if (!isValidCustomName(filename))
            throw new IllegalArgumentException("Invalid custom name");
        String timeString = DateUtil.getString(time);
        return BACKUP_FILENAME_PREFIX + "-" + timeString + "_" + filename + ".kbi";
    }

    @Override
    public BackupFilenameEncoder.BackupBasicInformation decode(String filename) {
        Pattern pattern = Pattern.compile(
                "^" + BACKUP_FILENAME_PREFIX + "-" + "([0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2})_(.+)\\.kbi" + "$"
        );
        Matcher matcher = pattern.matcher(filename);
        if (matcher.find()) {
            String timeString = matcher.group(1);
            String customName = matcher.group(2);
            return new BackupFilenameEncoder.BackupBasicInformation(customName, DateUtil.toLocalDateTime(timeString));
        }
        return null;
    }

}
