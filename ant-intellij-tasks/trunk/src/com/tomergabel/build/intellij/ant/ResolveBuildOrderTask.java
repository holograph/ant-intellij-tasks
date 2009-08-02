package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.ResolutionException;
import static com.tomergabel.util.CollectionUtils.join;
import static com.tomergabel.util.CollectionUtils.map;
import org.apache.tools.ant.BuildException;

import java.util.Collection;

public class ResolveBuildOrderTask extends ProjectTaskBase {
    protected static final String LIST_SEPARATOR = ",";
    protected String property;
    protected ResolutionModes mode = ResolutionModes.names;

    public ResolutionModes getMode() {
        return this.mode;
    }

    public void setMode( final ResolutionModes mode ) throws IllegalArgumentException {
        if ( mode == null )
            throw new IllegalArgumentException( "Mode value cannot be null." );

        this.mode = mode;
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty( final String property ) {
        this.property = property;
    }

    @Override
    protected void executeTask() throws BuildException {
        if ( this.property == null )
            throw new BuildException( "Target property (attribute 'property') not specified." );

        assertProjectSpecified();

        // Resolve build order
        final Collection<Module> buildOrder;
        try {
            buildOrder = projectResolver().resolveModuleBuildOrder();
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }

        // Set the target property
        getProject().setProperty( this.property, join( map( buildOrder, this.mode.mapper ), LIST_SEPARATOR ) );
    }
}
