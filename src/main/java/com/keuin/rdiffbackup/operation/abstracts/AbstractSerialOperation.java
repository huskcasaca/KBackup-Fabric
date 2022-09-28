package com.keuin.rdiffbackup.operation.abstracts;

/**
 * The most basic operation abstraction.
 * This class represents an serial operation, which is limited in a non-public method.
 * Note that the operation is not invokable by default, you should use InvokableOperation in order to provide a public method for users to call.
 */
public abstract class AbstractSerialOperation {
    /**
     * Do your operation here.
     * This method is not designed to be public.
     * When this method returns, the operation must have been finished.
     *
     * @return whether the operation succeed.
     */
    protected abstract boolean operate();
}
