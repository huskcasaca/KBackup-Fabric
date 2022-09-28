package com.keuin.rdiffbackup.operation.abstracts;

import com.keuin.rdiffbackup.operation.abstracts.i.Invokable;

public abstract class InvokableOperation extends AbstractSerialOperation implements Invokable {
    public boolean invoke() {
        return operate();
    }
}
