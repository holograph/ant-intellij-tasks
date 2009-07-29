package com.tomergabel.build.intellij.model;

public class ParseException extends ModelException {
    public ParseException() {
        super();
    }

    public ParseException( final String message ) {
        super( message );
    }

    public ParseException( final String message, final Throwable cause ) {
        super( message, cause );
    }

    public ParseException( final Throwable cause ) {
        super( cause );
    }
}
