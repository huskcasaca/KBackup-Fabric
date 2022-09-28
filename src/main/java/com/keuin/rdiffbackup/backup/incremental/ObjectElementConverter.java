package com.keuin.rdiffbackup.backup.incremental;

import com.keuin.rdiffbackup.backup.incremental.identifier.Sha256IdentifierConverter;
import com.keuin.rdiffbackup.util.backup.incremental.identifier.Sha256Identifier;

public class ObjectElementConverter {
    public static ObjectElement convert(com.keuin.rdiffbackup.util.backup.incremental.ObjectElement oldObjectElement) {
        try {
            return new ObjectElement(
                    oldObjectElement.getName(),
                    // in real world case, Sha256Identifier is the only used identifier in Rdiff Backup. So the cast is surely safe
                    Sha256IdentifierConverter.convert((Sha256Identifier) oldObjectElement.getIdentifier())
            );
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
