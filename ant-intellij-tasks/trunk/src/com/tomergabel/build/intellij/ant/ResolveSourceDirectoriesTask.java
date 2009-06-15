package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.build.intellij.model.Resolver;
import static com.tomergabel.util.CollectionUtils.join;
import static com.tomergabel.util.CollectionUtils.map;
import com.tomergabel.util.Mapper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class ResolveSourceDirectoriesTask extends ModuleTaskBase {

    // See http://ant.apache.org/manual/develop.html#set-magic
    // Ant supports enums as of 1.7.0, but requires case-sensitive matching.
    // Until this is improved, modes are declared lower-cased. --TG
    public enum Filter {
        source,
        test,
        both
    }

    protected String property;
    protected String pathId;
    protected Filter filter = Filter.both;

    public String getProperty() {
        return property;
    }

    public void setProperty( final String property ) {
        this.property = property;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId( final String pathId ) {
        this.pathId = pathId;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public void setFilter( final Filter filter ) {
        if ( filter == null )
            throw new IllegalArgumentException( "Filter cannot be null." );

        this.filter = filter;
    }

    @Override
    public void execute() throws BuildException {
        if ( this.property == null && this.pathId == null ) {
            error( "Target property or path (attributes 'property' and 'pathid' respectively) not specified." );
            return;
        }

        // Resolve source directories
        final Collection<String> sourceDirectories = resolveSourceDirectories();
        if ( sourceDirectories == null )
            return;

        // Set target property, if specified
        if ( this.property != null )
            getProject().setProperty( this.property, join( sourceDirectories, "," ) );

        // Set target path ID, if specified
        if ( this.pathId != null ) {
            final Path path = new Path( getProject() );
            for ( String sourceDirectory : sourceDirectories )
                path.append( new Path( getProject(), sourceDirectory ) );
            getProject().addReference( this.pathId, path );
        }
    }

    public Collection<String> resolveSourceDirectories() throws BuildException {
        return resolveSourceDirectories( this.filter );
    }

    public Collection<String> resolveSourceDirectories( Filter filter )
            throws IllegalArgumentException, BuildException {
        if ( filter == null )
            throw new IllegalArgumentException( "The filter cannot be null." );

        // Load module and create resolver
        final Module module = module();
        if ( module == null )
            return null;
        final Resolver resolver = resolver();

        // Create directory set and URI string resolution mapper
        final Collection<String> directories = new HashSet<String>();
        final Mapper<String, String> mapper = new Mapper<String, String>() {
            @Override
            public String map( final String source ) throws BuildException {
                try {
                    return new File( resolver.resolveUriString( source ) ).getAbsolutePath();
                } catch ( ResolutionException e ) {
                    throw new BuildException( "Cannot resolve source directory for \"" + source + "\"", e );
                }
            }
        };

        // Add the appropriate lists according to the filter
        try {
            if ( filter == Filter.source || filter == Filter.both )
                directories.addAll( map( module.getSourceUrls(), mapper ) );
            if ( filter == Filter.test || filter == Filter.both )
                directories.addAll( map( module.getTestSourceUrls(), mapper ) );
        } catch ( BuildException e ) {
            error( e );
            return null;
        }

        // Return the collection
        return Collections.unmodifiableCollection( directories );
    }
}
