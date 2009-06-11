package com.tomergabel.build.ant.tasks;

import com.tomergabel.build.intellij.Module;
import com.tomergabel.build.intellij.ParseException;
import com.tomergabel.build.intellij.Project;
import com.tomergabel.build.intellij.Resolver;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.IOException;

public class BuildModuleTask extends Task {
    protected File moduleDescriptor;
    protected File projectDescriptor;
    protected boolean failOnError;

    public void setSrcfile( final File srcfile ) {
        this.moduleDescriptor = srcfile;
    }

    public void setProject( final File project ) {
        this.projectDescriptor = project;
    }

    public void setFailonerror( final boolean failonerror ) {
        this.failOnError = failonerror;
    }

    private boolean ensure( boolean condition, String message ) {
        return condition || error( message );
    }

    private boolean error( final String message ) {
        return error( message, null );
    }

    private boolean error( final String message, final Throwable cause ) {
        if ( this.failOnError )
            throw new BuildException( message, cause );
        // TODO log warning
        return false;
    }

    @Override
    public void execute() throws BuildException {
        if ( !ensure( this.moduleDescriptor != null, "Module descriptor (attribute \"srcfile\") not specified." ) )
            return;

        // Load module descriptor and, optionally, project descriptor
        Module module = null;
        try {
            module = Module.parse( this.moduleDescriptor );
        } catch ( IOException e ) {
            if ( !error( "Failed to read module file " + this.moduleDescriptor, e ) )
                return;
        } catch ( ParseException e ) {
            if ( !error( "Error parsing module file " + this.moduleDescriptor, e ) )
                return;
        }
        Project project = null;
        if ( this.projectDescriptor != null  )
            try {
                project = Project.parse( this.projectDescriptor );
            } catch ( IOException e ) {
                if ( !error( "Failed to read project file " + this.projectDescriptor, e ) )
                    return;
            } catch ( ParseException e ) {
                if ( !error( "Error parsing project file " + this.projectDescriptor, e ) )
                    return;
            }
        final Resolver resolver = new Resolver( project, module );

        // Resolve output directory
        //module.getOutputUrl()

        // resolve module dependencies
        // build module dependencies
        // resolve classpath
        // build module sources
        // copy module resources                   
    }
}
