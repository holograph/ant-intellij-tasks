package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.ModelException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class TaskBase extends Task {
    protected boolean failOnError = true;
    private boolean executed = false;

    // Ant-facing properties

    // See http://ant.apache.org/manual/develop.html#writingowntask
    // Ant convention specifies property setters should have first letter capitalized
    // and the rest lower-cased. Go blame them. --TG
    public void setFailonerror( final boolean failonerror ) {
        this.failOnError = failonerror;
    }

    public static final String NEWLINE_SEPARATOR = System.getProperty( "line.separator" );

    public String formatErrorMessage( Throwable error ) {
        if ( error == null )
            throw new IllegalArgumentException( "The error cannot be null." );

        final StringWriter sw = new StringWriter();
        boolean first = true;
        while ( error != null ) {
            if ( first )
                first = false;
            else {
                sw.write( NEWLINE_SEPARATOR );
                sw.write( "Caused by: " );
            }

            if ( !( error instanceof ModelException ) && !( error instanceof BuildException ) ) {
                error.printStackTrace( new PrintWriter( sw, true ) );
                break;
            }

            sw.write( error.getMessage() );
            error = error.getCause();
        }

        return sw.toString();
    }

    @Override
    public final void execute() throws BuildException {
        assertNotExecuted();
        this.executed = true;
        try {
            executeTask();
        } catch ( Exception e ) {
            final String message = formatErrorMessage( e );
            if ( this.failOnError )
                throw new BuildException( message );
            else
                // Workaround for Ant bug https://issues.apache.org/bugzilla/show_bug.cgi?id=47623
                this.log( message, Project.MSG_ERR );
        }
    }

    protected void assertNotExecuted() throws IllegalStateException {
        if ( this.executed )
            throw new IllegalStateException( "Task has already been executed." );
    }

    protected abstract void executeTask() throws BuildException;
}
