package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Project;
import com.tomergabel.util.CollectionUtils;
import com.tomergabel.util.Predicate;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class AntUtils {
    private AntUtils() {}


    static String[] generateResourceIncludes( final Project project ) {
        if ( project == null )
            throw new IllegalArgumentException( "The project cannot be null." );

        // Derive resource patterns from project
        final String[] includes = new String[project.getResourceExtensions().size() +
                project.getResourceWildcardPatterns().size()];

        // Process resource extensions
        int i = 0;
        for ( final String extension : project.getResourceExtensions() )
            includes[ i++ ] = "**/*." + extension;

        // Process wildcard patterns
        for ( final String pattern : project.getResourceWildcardPatterns() )
            includes[ i++ ] = "**/" + pattern;
        return includes;
    }

    protected static String stripPreceedingSlash( final String uri ) {
        return uri != null ? ( uri.startsWith( "/" ) ? uri.substring( 1 ) : uri ) : null;
    }

    public static class SingletonResource implements ResourceCollection {
        private final Resource resource;

        public SingletonResource( final Resource resource ) {
            if ( resource == null )
                throw new IllegalArgumentException( "The resource cannot be null." );
            this.resource = resource;
        }

        @Override
        public Iterator iterator() {
            return Collections.singleton( this.resource ).iterator();
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isFilesystemOnly() {
            return resource.isFilesystemOnly();
        }

        public Resource getResource() {
            return this.resource;
        }
    }

    public static class ResourceContainer implements ResourceCollection {
        final Collection<Resource> resources = new ArrayList<Resource>();

        @Override
        public Iterator iterator() {
            return resources.iterator();
        }

        @Override
        public int size() {
            return resources.size();
        }

        @Override
        public boolean isFilesystemOnly() {
            return CollectionUtils.all( this.resources, new Predicate<Resource>() {
                @Override
                public boolean evaluate( final Resource x ) {
                    return x.isFilesystemOnly();
                }
            } );
        }

        public void add( final Resource resource ) {
            this.resources.add( resource );
        }
    }

    public static Resource mapFileResource( final File file, final String targetPrefix ) {
        if ( file == null )
            throw new IllegalArgumentException( "The file cannot be null." );
        if ( targetPrefix == null )
            throw new IllegalArgumentException( "The target prefix cannot be null." );

        return new FileResource( file ) {
            @Override
            public String getName() {
                return targetPrefix + File.separator + file.getName();
            }
        };
    }
}
