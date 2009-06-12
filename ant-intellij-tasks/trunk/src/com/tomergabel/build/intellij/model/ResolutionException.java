package com.tomergabel.build.intellij.model;

public class ResolutionException extends Exception {
    public String property;

    public String getProperty() {
        return property;
    }

    public void setProperty( final String property ) {
        this.property = property;
    }

    public ResolutionException() {
    }

    public ResolutionException( final String message ) {
        super( message );
    }

    public ResolutionException( final String message, final Throwable cause ) {
        super( message, cause );
    }

    public ResolutionException( final Throwable cause ) {
        super( cause );
    }
}
