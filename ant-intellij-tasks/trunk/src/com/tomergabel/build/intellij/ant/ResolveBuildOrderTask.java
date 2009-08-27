package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.ResolutionException;
import static com.tomergabel.util.CollectionUtils.join;
import static com.tomergabel.util.CollectionUtils.map;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

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

        final String[] split = modules.split( LIST_SEPARATOR );
        final Collection<String> list = new HashSet<String>( split.length );
        for ( final String module : split )
            if ( module.length() > 0 )
                list.add( module );
        this.modules = list.size() > 0 ? list : null;
    }

    @Override
    protected void executeTask() throws BuildException {
        if ( this.property == null )
            throw new BuildException( "Target property (attribute 'property') not specified." );

        assertProjectSpecified();

        // Resolve build order
        final Collection<Module> buildOrder;
        try {
            if ( this.modules == null || this.modules.size() == 0 ) {
                logVerbose( "Resolving build order for project \"%s\"", project().getName() );
                buildOrder = projectResolver().resolveModuleBuildOrder();
            } else {
                logVerbose( "Resolving build order for modules: %s", join( this.modules ) );
                switch ( this.inputMode ) {
                    case names:
                        buildOrder = projectResolver().resolveModuleBuildOrderByName( this.modules );
                        break;

                    case descriptors:
                        buildOrder = new ArrayList<Module>();   // Make sure order is maintained
                        for ( final String descriptor : this.modules )
                            buildOrder
                                    .add( projectResolver().getModule(
                                            projectResolver().resolveUriString( descriptor ) ) );
                        break;

                    default:
                        // Safety net, should never happen
                        throw new IllegalStateException( "Unknown input mode '" + this.inputMode + "'" );
                }
            }
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }

        // Set the target property
        final String value = join(
                map( buildOrder, ( this.outputMode == null ? this.inputMode : this.outputMode ).mapper ),
                LIST_SEPARATOR );
        getProject().setProperty( this.property, value );
        logVerbose( "Resolved build order: %s", value );
    }
}
