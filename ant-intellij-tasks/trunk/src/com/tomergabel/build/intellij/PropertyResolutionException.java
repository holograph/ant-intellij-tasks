package com.tomergabel.build.intellij;

public class PropertyResolutionException extends Exception {
    public final String property;

    public PropertyResolutionException( final String property ) {
        super( "Failed to resolve property \"" + property + "\"" );
        this.property = property;
    }

    public PropertyResolutionException( final String message, final String property ) {
        super( message );
        this.property = property;
    }

    public PropertyResolutionException( final String message, final Throwable cause, final String property ) {
        super( message, cause );
        this.property = property;
    }

    public PropertyResolutionException( final Throwable cause, final String property ) {
        super( "Failed to resolve property \"" + property + "\"", cause );
        this.property = property;
    }
}
