package com.tomergabel.util;

import java.util.concurrent.Callable;

public abstract class Lazy<T> implements Callable<T> {
    private T value;

    protected Lazy() {
    }

    protected Lazy( final T value ) {
        this.value = value;
    }

    public T get() throws LazyInitializationException {
        try {
            return this.value == null ? ( this.value = this.call() ) : this.value;
        } catch ( Exception e ) {
            throw new LazyInitializationException( e );
        }
    }

    public static <T> Lazy<T> from( T value ) {
        return new Lazy<T>( value ) {
            @Override
            public T call() throws Exception {
                // Safety net, should never happen
                throw new IllegalStateException( "Lazy initializer should be not called on lazy constants." );
            }
        };
    }

    public static <T> Lazy<T> from( final Callable<T> initializer ) {
        return new Lazy<T>() {
            @Override
            public T call() throws Exception {
                return initializer.call();
            }
        };
    }
}
