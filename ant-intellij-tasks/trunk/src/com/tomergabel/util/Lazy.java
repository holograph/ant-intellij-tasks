package com.tomergabel.util;

import java.util.concurrent.Callable;

/**
 * Utility class that provides a lazy initialization object wrapper.
 * <p/>
 * To use this wrapper simply implement {@link java.util.concurrent.Callable#call()} and return the appropriate value.
 * Exceptions are propagated by {@link #get()} as {@link com.tomergabel.util.LazyInitializationException}s.
 * <p/>
 * <strong>Note: This wrapper is <em>not</em> thread-safe!</strong>
 *
 * @param <T> The type wrapped by the lazy initializer.
 */
public abstract class Lazy<T> implements Callable<T> {
    /**
     * The actual value.
     */
    private T value = null;

    /**
     * Protected c'tor (to prevent direct initialization.
     */
    protected Lazy() {
    }

    /**
     * A private c'tor used to pre-cache the value. This is used by the convenience method {@link #from(Object)} to
     * create a "const" lazy initializer.
     *
     * @param value The actual value.
     */
    private Lazy( final T value ) {
        this.value = value;
    }

    /**
     * Retreives the lazy-loaded value, initializing it on the fly if necessary.
     *
     * @return The lazy-loaded value.
     * @throws LazyInitializationException An error has occurred while initializing the lazy-loaded value. Please see
     *                                     the exception {@link Exception#getCause() cause} for detials.
     */
    public T get() throws LazyInitializationException {
        try {
            return this.value == null ? ( this.value = this.call() ) : this.value;
        } catch ( Exception e ) {
            throw new LazyInitializationException( e );
        }
    }

    /**
     * Wraps a value with a pre-cached value. This is a convenience method intended to provide an easy way to specify
     * constant values to a lazy-loaded placeholder if necessary.
     *
     * @param value The actual value.
     * @param <T>   The type wrapped by the lazy initializer.
     * @return A {@link Lazy} instance for the specified value.
     */
    public static <T> Lazy<T> from( final T value ) {
        return new Lazy<T>( value ) {
            @Override
            public T call() throws Exception {
                // Safety net, should never happen
                throw new IllegalStateException( "Lazy initializer should be not called on lazy constants." );
            }
        };
    }

    /**
     * Wraps the specified callable with a {@link Lazy} instance. This is an alternative to extending this class.
     *
     * @param initializer The initializer. Calling {@link #from} with {@literal null} for an argument will resolve here,
     *                    so specifying a {@literal null} initializer is the same as specifying a callable which returns
     *                    {@literal null}.
     * @return A {@link Lazy} instance for the specified initializer.
     */
    @SuppressWarnings( { "unchecked" } )
    public static <T> Lazy<T> from( final Callable<T> initializer ) {
        return new Lazy<T>() {
            @Override
            public T call() throws Exception {
                // Lazy.from( null ) will resolve here, so (as a convenience) we support
                // return of null values
                return initializer == null ? null : initializer.call();
            }
        };
    }
}
