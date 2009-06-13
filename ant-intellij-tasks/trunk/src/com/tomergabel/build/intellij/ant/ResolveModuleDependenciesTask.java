package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.ResolutionException;
import static com.tomergabel.util.CollectionUtils.join;
import static com.tomergabel.util.CollectionUtils.map;
import com.tomergabel.util.Mapper;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.Collection;

public class ResolveModuleDependenciesTask extends ModuleTaskBase {
    protected static final String LIST_SEPARATOR = ",";

    public enum Modes {
        NAMES,
        DESCRIPTORS
    }

    protected String property;
    protected Modes mode = Modes.NAMES;

    public Modes getMode() {
        return mode;
    }

    public void setMode( final Modes mode ) throws IllegalArgumentException {
        if ( mode == null )
            throw new IllegalArgumentException( "Mode value cannot be null." );

        this.mode = mode;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty( final String property ) {
        this.property = property;
    }

    @Override
    public void execute() throws BuildException {
        if ( this.property == null ) {
            error( "Target property (attribute 'property') not specified." );
            return;
        }

        // Resolve module dependencies
        final Collection<Module> modules;
        try {
            modules = resolveModules();
        } catch ( BuildException e ) {
            error( e );
            return;
        }

        // Build the correct mapper for the specified mode
        final Mapper<Module, ?> mapper;
        switch ( this.mode ) {
            case NAMES:
                mapper = new Mapper<Module, Object>() {
                    @Override
                    public Object map( final Module source ) {
                        return source.getName();
                    }
                };
                break;

            case DESCRIPTORS:
                mapper = new Mapper<Module, Object>() {
                    @Override
                    public Object map( final Module source ) {
                        return new File( source.getDescriptor() ).getAbsolutePath();
                    }
                };
                break;

            default:
                // Safety net, should never happen
                throw new IllegalStateException( "Unknown mode \"" + this.mode + "\"." );
        }

        // Set the target property
        getProject().setProperty( this.property, join( map( modules, mapper ), ',' ) );
    }

    public Collection<Module> resolveModules() throws BuildException {
        try {
            return resolver().resolveModuleDependencies();
        } catch ( ResolutionException e ) {
            error( "Failed to resolve module dependencies.", e );
            return null;
        }
    }
}
