package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.ResolutionException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.io.File;

public class ResolveOutputDirectoryTask extends ModuleTaskBase {

    protected String property;

    public void setProperty( final String property ) {
        this.property = property;
    }

    @Override
    public void execute() throws BuildException {
        if ( this.property == null ) {
            error( "Target property (attribute 'property') not specified." );
            return;
        }

        final String outputDirectory = resolveOutputDirectory();
        if ( null == outputDirectory )
            return;

        getProject().setProperty( this.property, outputDirectory );
    }

    public String resolveOutputDirectory() throws BuildException {
        try {
            String outputUrl = module().getOutputUrl();
            if ( outputUrl == null ) {
                if ( project() == null ) {
                    error( "Module does not specify an output directory and project was not specified." );
                    return null;
                }

                log( "Module does not define an output directory, falling back to project settings",
                        Project.MSG_VERBOSE );
                outputUrl = project().getOutputUrl();
                if ( outputUrl == null ) {
                    error( "Module does not specify and output directory and the project does not specify " +
                            "a default." );
                    return null;
                }
            }

            log( "Attempting to resolve output URL '" + outputUrl + "'", Project.MSG_VERBOSE );
            final String path = new File( resolver().resolveUriString( outputUrl ) ).getAbsolutePath();
            log( "Output URL resolved to '" + path + "'", Project.MSG_VERBOSE );
            return path;
        } catch ( ResolutionException e ) {
            error( "Failed to resolve output directory.", e );
            return null;
        }
    }
}
