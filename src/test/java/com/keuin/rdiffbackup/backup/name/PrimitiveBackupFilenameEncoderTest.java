package com.keuin.rdiffbackup.backup.name;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.*;

public class PrimitiveBackupFilenameEncoderTest {

    @Test
    public void testConsistency() {
        LocalDateTime time = LocalDateTime.ofEpochSecond(System.currentTimeMillis() / 1000, 0, ZoneOffset.UTC);
        String name = "Test Na_me";
        PrimitiveBackupFilenameEncoder encoder = PrimitiveBackupFilenameEncoder.INSTANCE;
        BackupFilenameEncoder.BackupBasicInformation information = encoder.decode(encoder.encode(name, time));
        assertEquals(time, information.time);
        assertEquals(name, information.customName);
    }

    @Test
    public void testEncode() {
        LocalDateTime time = LocalDateTime.of(1, 1, 1, 1, 1, 1);
        String customName = "name";
        PrimitiveBackupFilenameEncoder encoder = PrimitiveBackupFilenameEncoder.INSTANCE;
        assertEquals("backup-0001-01-01_01-01-01_name.zip", encoder.encode(customName, time));
    }

    @Test
    public void testDecode() {
        LocalDateTime time = LocalDateTime.of(1, 1, 1, 1, 1, 1);
        String customName = "name";
        PrimitiveBackupFilenameEncoder encoder = PrimitiveBackupFilenameEncoder.INSTANCE;
        BackupFilenameEncoder.BackupBasicInformation information = encoder.decode("backup-0001-01-01_01-01-01_name.zip");
        assertEquals(time, information.time);
        assertEquals(customName, information.customName);
    }

    @Test
    public void isValid() {
        PrimitiveBackupFilenameEncoder encoder = PrimitiveBackupFilenameEncoder.INSTANCE;
        assertTrue(encoder.isValidFilename("backup-0001-01-01_01-01-01_name.zip"));
        assertTrue(encoder.isValidFilename("backup-0001-01-01_01-01-01_0001-01-01_01-01-01_name.zip"));
        assertFalse(encoder.isValidFilename("backup-0001-01-01_01-01-01kbackup-0001-01-01_01-01-01_name.zip"));
        assertFalse(encoder.isValidFilename("backup-0001-01-01_01-01-01_name"));
        assertFalse(encoder.isValidFilename("backup-0001-01-01_01-01-01_name.kbi"));
        assertFalse(encoder.isValidFilename("somefile"));
    }
}