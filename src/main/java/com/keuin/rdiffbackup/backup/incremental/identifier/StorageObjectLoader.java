package com.keuin.rdiffbackup.backup.incremental.identifier;

import java.io.File;
import java.util.Objects;

public class StorageObjectLoader {
    /**
     * Get identifier from storage file.
     *
     * @param file storage file.
     * @return identifier. If failed, return null.
     */
    public static ObjectIdentifier asIdentifier(File file) {
        Objects.requireNonNull(file);
        String fileName = file.getName();
        ObjectIdentifier identifier;

        identifier = Sha256Identifier.fromFilename(fileName);
        return identifier;

        // Add more identifiers.
    }
}
