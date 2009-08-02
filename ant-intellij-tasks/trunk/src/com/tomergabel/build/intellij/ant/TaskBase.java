package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

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

//    protected void error( final String message ) throws BuildException {
//        error( message, null );
//    }
//
//    protected void error( final Throwable cause ) throws BuildException {
//        error( null, cause );
//    }
//
//    protected void error( final String message, final Throwable cause ) throws BuildException {
//        if ( this.failOnError )
//            throw new BuildException( formatErrorMessage( message, cause ) );
//        else
//            this.log( message, cause, Project.MSG_ERR );
//    }
//
//    public static final String NEWLINE_SEPARATOR = System.getProperty( "line.separator" );
//
//    public String formatErrorMessage( final String message, Throwable cause ) {
//        final StringWriter sw = new StringWriter();
//        boolean first = true;
//        if ( message != null ) {
//            sw.write( message );
//            first = false;
//        }
//
//        while ( cause != null ) {
//            if ( first )
//                first = false;
//            else {
//                sw.write( NEWLINE_SEPARATOR );
//                sw.write( "Caused by: " );
//            }
//
//            if ( !( cause instanceof ModelException ) ) {
//                cause.printStackTrace( new PrintWriter( sw, true ) );
//                break;
//            }
//
//            sw.write( cause.getMessage() );
//            cause = cause.getCause();
//        }
//
//        return sw.toString();
//    }

    @Override
    public final void execute() throws BuildException {
        assertNotExecuted();
        this.executed = true;
        try {
            executeTask();
        } catch ( BuildException e ) {
            if ( this.failOnError )
                throw e;
            else
                logError( e );
        } catch ( Exception e ) {
            if ( this.failOnError )
                throw new BuildException( e );
            else
                logError( e );
        }
    }

    protected void logError( final Throwable error ) {
        // Workaround for Ant bug https://issues.apache.org/bugzilla/show_bug.cgi?id=47623
        if ( error.getMessage() == null )
            this.log( "", error, Project.MSG_ERR );
        else
            this.log( error, Project.MSG_ERR );
    }

    protected void assertNotExecuted() throws IllegalStateException {
        if ( this.executed )
            throw new IllegalStateException( "Task has already been executed." );
    }

    protected abstract void executeTask() throws BuildException;
}
