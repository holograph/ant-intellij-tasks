package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.ResolutionException;
import static com.tomergabel.util.CollectionUtils.join;
import static com.tomergabel.util.CollectionUtils.map;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ResolveBuildOrderTask extends ProjectTaskBase {
    protected static final String LIST_SEPARATOR = ",";
    protected String property;
    protected Collection<String> modules;
    protected ResolutionModes inputMode = ResolutionModes.names;
    protected ResolutionModes outputMode = null;

    public ResolutionModes getInputMode() {
        return this.inputMode;
    }

    public void setInputMode( final ResolutionModes inputMode ) throws IllegalArgumentException {
        if ( inputMode == null )
            throw new IllegalArgumentException( "Mode value cannot be null." );

        this.inputMode = inputMode;
    }

    public ResolutionModes getOutputMode() {
        return this.outputMode;
    }

    public void setOutputMode( final ResolutionModes outputMode ) {
        if ( outputMode == null )
            throw new IllegalArgumentException( "Mode value cannot be null." );

        this.outputMode = outputMode;
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty( final String property ) {
        if ( property == null )
            throw new IllegalArgumentException( "The property name cannot be null." );

        this.property = property;
    }

    public void setModules( final String modules ) {
        if ( modules == null )
            throw new IllegalArgumentException( "The name list cannot be null." );

        this.modules = Arrays.asList( modules.split( LIST_SEPARATOR ) );
    }

    @Override
    protected void executeTask() throws BuildException {
        if ( this.property == null )
            throw new BuildException( "Target property (attribute 'property') not specified." );

        assertProjectSpecified();

        // Resolve build order
        final Collection<Module> buildOrder;
        try {
            if ( this.modules == null )
                buildOrder = projectResolver().resolveModuleBuildOrder();
            else switch ( this.inputMode ) {
                case names:
                    buildOrder = projectResolver().resolveModuleBuildOrderByName( this.modules );
                    break;

                case descriptors:
                    buildOrder = new ArrayList<Module>();   // Make sure order is maintained
                    for ( final String descriptor : this.modules )
                        buildOrder
                                .add( projectResolver().getModule( projectResolver().resolveUriString( descriptor ) ) );
                    break;

                default:
                    // Safety net, should never happen
                    throw new IllegalStateException( "Unknown input mode '" + this.inputMode + "'" );
            }
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }

        // Set the target property
        getProject().setProperty( this.property,
                join( map( buildOrder, ( this.outputMode == null ? this.inputMode : this.outputMode ).mapper ),
                        LIST_SEPARATOR ) );
    }
}
