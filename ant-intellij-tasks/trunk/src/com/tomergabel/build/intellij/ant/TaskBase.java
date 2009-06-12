package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class TaskBase extends Task {
    protected boolean failOnError;

    public void setFailonerror( final boolean failonerror ) {
        this.failOnError = failonerror;
    }

    protected void error( final String message ) throws BuildException {
        error( message, null );
    }

    protected void error( final Throwable cause ) throws BuildException {
        error( null, cause );
    }

    protected void error( final String message, final Throwable cause ) throws BuildException {
        if ( this.failOnError )
            throw new BuildException( message, cause );
    }
}
