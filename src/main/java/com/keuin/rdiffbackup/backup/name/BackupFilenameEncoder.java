package com.keuin.rdiffbackup.backup.name;

import com.keuin.rdiffbackup.util.DateUtil;

import java.time.LocalDateTime;

/**
 * Encode and decode backup file name for a specific backup type.
 */
public interface BackupFilenameEncoder {

    /**
     * Construct full backup file name from custom name and creation time.
     * @param filename the custom name. If the custom name contains invalid chars, an exception will be thrown.
     * @param time the creation time.
     * @return the file name.
     */
    String encode(String filename, LocalDateTime time);

    /**
     * Extract custom and backup time from backup file name.
     *
     * @param filename the backup file name.
     * @return the information. If the given file name is invalid, return null.
     */
    BackupBasicInformation decode(String filename);

    default boolean isValidFilename(String filename) {
        return decode(filename) != null;
    }

    /**
     * Check if the given string is a valid custom backup name.
     *
     * @param filename the custom backup name.
     * @return if the name is valid.
     */
    default boolean isValidCustomName(String filename) {
        final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
        for (char c : ILLEGAL_CHARACTERS) {
            if (filename.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    class BackupBasicInformation {

        public final String customName;
        public final LocalDateTime time;

        protected BackupBasicInformation(String customName, LocalDateTime time) {
            this.customName = customName;
            this.time = time;
        }

        @Override
        public String toString() {
            return String.format("%s, %s", customName, DateUtil.getString(time));
        }
    }
}
