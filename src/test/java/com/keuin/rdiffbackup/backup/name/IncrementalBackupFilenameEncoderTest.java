package com.keuin.rdiffbackup.backup.name;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class IncrementalBackupFilenameEncoderTest {
    @Test
    public void testEncode() {
        LocalDateTime time = LocalDateTime.of(1, 1, 1, 1, 1, 1);
        String customName = "name";
        IncrementalBackupFilenameEncoder encoder = IncrementalBackupFilenameEncoder.INSTANCE;
        assertEquals("incremental-0001-01-01_01-01-01_name.kbi", encoder.encode(customName, time));
    }

    @Test
    public void testDecode() {
        LocalDateTime time = LocalDateTime.of(1, 1, 1, 1, 1, 1);
        String customName = "name";
        IncrementalBackupFilenameEncoder encoder = IncrementalBackupFilenameEncoder.INSTANCE;
        BackupFilenameEncoder.BackupBasicInformation information = encoder.decode("incremental-0001-01-01_01-01-01_name.kbi");
        assertEquals(time, information.time);
        assertEquals(customName, information.customName);
    }

    @Test
    public void isValid() {
        IncrementalBackupFilenameEncoder encoder = IncrementalBackupFilenameEncoder.INSTANCE;
        assertTrue(encoder.isValidFilename("incremental-0001-01-01_01-01-01_name.kbi"));
        assertTrue(encoder.isValidFilename("incremental-0001-01-01_01-01-01_0001-01-01_01-01-01_name.kbi"));
        assertFalse(encoder.isValidFilename("incremental-0001-01-01_01-01-01incremental-0001-01-01_01-01-01_name.kbi"));
        assertFalse(encoder.isValidFilename("incremental-0001-01-01_01-01-01_name"));
        assertFalse(encoder.isValidFilename("incremental-0001-01-01_01-01-01_name.zip"));
        assertFalse(encoder.isValidFilename("somefile"));
    }
}