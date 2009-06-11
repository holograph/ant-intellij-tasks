package com.tomergabel.util;

public class LazyInitializationException extends Exception {
    public LazyInitializationException() {
    }

    public LazyInitializationException( final String message ) {
        super( message );
    }

    public LazyInitializationException( final String message, final Throwable cause ) {
        super( message, cause );
    }

    public LazyInitializationException( final Throwable cause ) {
        super( cause );
    }
}
