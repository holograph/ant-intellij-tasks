package com.tomergabel.util;

public class LazyInitializationException extends Exception {
    public LazyInitializationException( final String message, final Throwable cause ) {
        super( message, cause );
    }

    public LazyInitializationException( final Throwable cause ) {
        super( cause );
    }
}
