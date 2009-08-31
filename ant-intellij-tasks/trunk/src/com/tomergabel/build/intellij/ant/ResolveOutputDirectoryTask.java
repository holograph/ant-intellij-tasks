package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.ResolutionException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class ResolveOutputDirectoryTask extends ModuleTaskBase {
    public enum Mode {
        test,
        source
    }

    protected String property;
    protected Mode mode = Mode.source;

    public void setProperty( final String property ) {
        this.property = property;
    }

    public void setMode( final Mode mode ) {
        if ( mode == null )
            throw new IllegalArgumentException( "The mode cannot be null." );

        this.mode = mode;
    }

    public String getProperty() {
        return this.property;
    }

    public Mode getMode() {
        return this.mode;
    }

    @Override
    public void executeTask() throws BuildException {
        if ( this.property == null )
            throw new BuildException( "Target property (attribute 'property') not specified." );

        log( "Attempting to resolve output URL '" +
                ( module().getOutputUrl() != null ? module().getOutputUrl() : "null" ) + "'", Project.MSG_VERBOSE );
        final String outputDirectory;
        try {
            outputDirectory = resolver().resolveModuleOutputPath( this.mode == Mode.test );
        } catch ( ResolutionException e ) {
            throw new BuildException( "Failed to resolve module output directory.", e );
        }

        log( "Output URL resolved to '" + outputDirectory + "'", Project.MSG_VERBOSE );
        getProject().setProperty( this.property, outputDirectory );
    }
}
