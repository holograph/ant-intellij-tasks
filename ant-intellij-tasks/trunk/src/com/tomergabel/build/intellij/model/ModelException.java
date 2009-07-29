package com.tomergabel.build.intellij.model;

public class ModelException extends Exception {
    public ModelException() {
        super();
    }

    public ModelException( final String message ) {
        super( message );
    }

    public ModelException( final String message, final Throwable cause ) {
        super( message, cause );
    }

    public ModelException( final Throwable cause ) {
        super( cause );
    }
}
