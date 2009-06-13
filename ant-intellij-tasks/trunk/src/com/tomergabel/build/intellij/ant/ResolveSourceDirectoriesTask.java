package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.build.intellij.model.Resolver;
import static com.tomergabel.util.CollectionUtils.join;
import static com.tomergabel.util.CollectionUtils.map;
import com.tomergabel.util.Mapper;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class ResolveSourceDirectoriesTask extends ModuleTaskBase {

    protected String property;
    protected boolean includeTestDirectories = true;

    public boolean isIncludeTestDirectories() {
        return includeTestDirectories;
    }

    public void setIncludeTestDirectories( final boolean includeTestDirectories ) {
        this.includeTestDirectories = includeTestDirectories;
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

        final Collection<String> sourceDirectories = resolveSourceDirectories();
        if ( null == sourceDirectories )
            return;

        getProject().setProperty( this.property, join( sourceDirectories, "," ) );
    }

    public Collection<String> resolveSourceDirectories() throws BuildException {
        final Resolver resolver = resolver();
        final Module module = module();
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

        try {
            directories.addAll( map( module.getSourceUrls(), mapper ) );
            if ( this.includeTestDirectories )
                directories.addAll( map( module.getTestSourceUrls(), mapper ) );
            return Collections.unmodifiableCollection( directories );
        } catch ( BuildException e ) {
            error( e );
            return null;
        }
    }
}
