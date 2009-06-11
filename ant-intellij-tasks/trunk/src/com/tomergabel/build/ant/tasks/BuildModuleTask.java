package com.tomergabel.build.ant.tasks;

import com.tomergabel.build.intellij.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

public class BuildModuleTask extends Task {
    private URI moduleDescriptor;
    private URI projectDescriptor;
    private boolean failOnError;
    private Module module;

    public void setSrcfile( final File srcfile ) {
        this.moduleDescriptor = srcfile.toURI();
    }

    public void setProject( final File project ) {
        this.projectDescriptor = project.toURI();
    }

    public void setFailonerror( final boolean failonerror ) {
        this.failOnError = failonerror;
    }

    public void setModule( final Module module ) {
        this.module = module;
    }

    private void error( final String message ) {
        error( message, null );
    }

    private void error( final String message, final Throwable cause ) {
        if ( this.failOnError )
            throw new BuildException( message, cause );
    }

    @Override
    public void execute() throws IllegalStateException, BuildException {
        if ( this.moduleDescriptor != null ) {
            error( "Module descriptor (attribute \"srcfile\") not specified." );
            return;
        }

        // Load module and project descriptors
        final Module module;
        try {
            module = this.module != null ? this.module : Module.parse( this.moduleDescriptor );
        } catch ( IOException e ) {
            error( "Failed to read module file " + this.moduleDescriptor, e );
            return;
        } catch ( ParseException e ) {
            error( "Error parsing module file " + this.moduleDescriptor, e );
            return;
        }
        final Project project;
        try {
            project = this.projectDescriptor != null ? Project.parse( this.projectDescriptor ) : null;
        } catch ( IOException e ) {
            error( "Failed to read project file " + this.projectDescriptor, e );
            return;
        } catch ( ParseException e ) {
            error( "Error parsing project file " + this.projectDescriptor, e );
            return;
        }

        // Create resolver
        final Resolver resolver;
        try {
            resolver = new Resolver( project, module );
        } catch ( ResolutionException e ) {
            error( "Error parsing project file " + this.projectDescriptor, e );
            return;
        }

        // Resolve output directory
        final URI output;
        try {
            output = resolver.resolveUri( module.getOutputUrl() );
        } catch ( ResolutionException e ) {
            error( "Failed to resolve output directory", e );
            return;
        }


        //resolver.resolveDependencies( module, project,  );


        // build module dependencies
        // resolve classpath
        // build module sources
        // copy module resources                   
    }

    private Collection<BuildModuleTask> resolveDependencies( final Module module, final Project project,
                                                             final Collection<Task> buildTasks ) {
        for ( Dependency dependency : module.getDepdencies() ) {
            if ( dependency instanceof LibraryDependency ) {
                // TODO handle libraries
            } else if ( dependency instanceof ModuleDependency ) {
            }
        }

        return null;
    }
}
