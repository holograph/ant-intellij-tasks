package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.ResolutionException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class ResolveOutputDirectoryTask extends ModuleTaskBase {

    protected String property;

    public void setProperty( final String property ) {
        this.property = property;
    }

    @Override
    public void executeTask() throws BuildException {
        if ( this.property == null ) {
            error( "Target property (attribute 'property') not specified." );
            return;
        }

        log( "Attempting to resolve output URL '" +
                ( module().getOutputUrl() != null ? module().getOutputUrl() : "null" ) + "'", Project.MSG_VERBOSE );
        final String outputDirectory;
        try {
            outputDirectory = resolver().resolveModuleOutput();
        } catch ( ResolutionException e ) {
            error( "Failed to resolve module output directory.", e );
            return;
        }

        log( "Output URL resolved to '" + outputDirectory + "'", Project.MSG_VERBOSE );
        getProject().setProperty( this.property, outputDirectory );
    }
}
