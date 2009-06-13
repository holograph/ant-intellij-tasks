package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.ResolutionException;
import org.apache.tools.ant.BuildException;

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
            return new File( resolver().resolveUri( module().getOutputUrl() ) ).getAbsolutePath();
        } catch ( ResolutionException e ) {
            error( "Failed to resolve output directory.", e );
            return null;
        }
    }
}
