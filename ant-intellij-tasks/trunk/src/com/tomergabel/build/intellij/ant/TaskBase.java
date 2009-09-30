/*
	Copyright 2009 Tomer Gabel <tomer@tomergabel.com>

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


	ant-intellij-tasks project (http://code.google.com/p/ant-intellij-tasks/)

	$Id$
*/

package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.ModelException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class TaskBase extends Task {
    protected boolean failOnError = true;
    private AntUtils antUtils;
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
        this.antUtils = new AntUtils( getProject() );
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

    protected void logVerbose( final String message, final Object... args ) {
        log( String.format( message, args ), Project.MSG_VERBOSE );
    }

    protected void logDebug( final String message, final Object... args ) {
        log( String.format( message, args ), Project.MSG_DEBUG );
    }

    protected void logWarn( final String message, final Object... args ) {
        log( String.format( message, args ), Project.MSG_WARN );
    }

    protected void logWarn( final Throwable cause, final String message, final Object... args ) {
        log( String.format( message, args ), cause, Project.MSG_WARN );
    }

    protected void logInfo( final String message, final Object... args ) {
        log( String.format( message, args ), Project.MSG_INFO );
    }

    protected AntUtils ant() {
        return this.antUtils;
    }
}
