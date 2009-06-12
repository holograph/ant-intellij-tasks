package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.ResolutionException;
import static com.tomergabel.util.CollectionUtils.join;
import static com.tomergabel.util.CollectionUtils.map;
import com.tomergabel.util.Mapper;
import org.apache.tools.ant.BuildException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ResolveModuleDependenciesTask extends ModuleTaskBase {
    protected static final String LIST_SEPARATOR = ",";

    public enum Modes {
        NAMES,
        DESCRIPTORS;

        private static Map<String, Modes> modeMap;

        static {
            modeMap = new HashMap<String, Modes>( Modes.values().length );
            for ( Modes mode : Modes.values() )
                modeMap.put( mode.toString().toLowerCase(), mode );
        }

        public static Modes parse( String mode ) throws IllegalArgumentException {
            if ( mode == null )
                throw new IllegalArgumentException( "Mode value cannot be null." );
            final Modes parsed = modeMap.get( mode.toLowerCase() );
            if ( parsed == null )
                throw new IllegalArgumentException( "Unknown mode \"" + mode + "\"" );
            return parsed;
        }
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

    public void setMode( final String mode ) throws IllegalArgumentException {
        this.mode = Modes.parse( mode );
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
        } catch ( ResolutionException e ) {
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
                        return source.getDescriptor();
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

    public Collection<Module> resolveModules() throws ResolutionException {
        return resolver().resolveModuleDependencies();
    }
}
