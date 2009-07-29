package com.tomergabel.build.intellij.model;

public class ResolutionException extends ModelException {
    private final String property;

    public String getProperty() {
        return this.property;
    }

    public ResolutionException() {
        super();
        this.property = null;
    }

    public ResolutionException( final String message ) {
        super( message );
        this.property = null;
    }

    public ResolutionException( final String message, final Throwable cause ) {
        super( message, cause );
        this.property = null;
    }

    public ResolutionException( final Throwable cause ) {
        super( cause );
        this.property = null;
    }

    public ResolutionException( final String property, final String message ) {
        super( message );
        this.property = property;
    }

    public ResolutionException( final String property, final String message, final Throwable cause ) {
        super( message, cause );
        this.property = property;
    }
}
