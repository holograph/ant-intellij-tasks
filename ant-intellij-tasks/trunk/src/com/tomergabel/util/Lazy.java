package com.tomergabel.util;

import java.util.concurrent.Callable;

public class Lazy<T> {
    private Callable<T> initializer;
    private T value;

    public Lazy( final Callable<T> initializer ) {
        this.value = null;
        this.initializer = initializer;
    }

    public Lazy( final T value ) {
        this.value = value;
    }

    public T get() throws LazyInitializationException {
        try {
            return this.value == null ? ( this.value = this.initializer.call() ) : this.value;
        } catch ( Exception e ) {
            throw new LazyInitializationException( e );
        }
    }
}
