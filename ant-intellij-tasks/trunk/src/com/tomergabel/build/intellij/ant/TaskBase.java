package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;

public class TaskBase extends Task {
    protected boolean failOnError = true;

    // Ant-facing tasks

    // See http://ant.apache.org/manual/develop.html#writingowntask
    // Ant convention specifies property setters should have first letter capitalized
    // and the rest lower-cased. Go blame them. --TG
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
        else
            this.log( message, cause, Project.MSG_ERR );
    }
}
