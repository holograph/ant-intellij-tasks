package com.tomergabel.build.intellij;

public class ParseException extends Exception {
    public ParseException() {
    }

    public ParseException( String message ) {
        super( message );
    }

    public ParseException( String message, Throwable cause ) {
        super( message, cause );
    }

    public ParseException( Throwable cause ) {
        super( cause );
    }
}
