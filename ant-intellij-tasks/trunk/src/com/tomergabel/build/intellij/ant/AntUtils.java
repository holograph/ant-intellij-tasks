package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.ModuleResolver;
import com.tomergabel.build.intellij.model.Project;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.util.CollectionUtils;
import com.tomergabel.util.PathUtils;
import com.tomergabel.util.Predicate;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Move;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class AntUtils {
    private final org.apache.tools.ant.Project project;

    public AntUtils( final org.apache.tools.ant.Project project ) {
        this.project = project;
    }

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

    public static Resource mapFileResource( final FileResource resource, final String targetPrefix ) {
        if ( resource == null )
            throw new IllegalArgumentException( "The file resource cannot be null." );
        if ( targetPrefix == null )
            throw new IllegalArgumentException( "The target prefix cannot be null." );

        return new FileResource( resource.getFile() ) {
            @Override
            public String getName() {
                return targetPrefix + File.separator +
                        PathUtils.relativize( resource.getBaseDir(), resource.getFile() );
            }
        };
    }

    public static Resource mapFileResource( final File file, final String targetLocation ) {
        if ( file == null )
            throw new IllegalArgumentException( "The file cannot be null." );
        if ( targetLocation == null )
            throw new IllegalArgumentException( "The target location cannot be null." );
        if ( !file.isAbsolute() )
            throw new IllegalArgumentException( "The file path must be absolute." );

        return new FileResource( file.getAbsoluteFile() ) {
            @Override
            public String getName() {
                return targetLocation;
            }
        };
    }

    public static Resource mapFileResource( final File root, final File target, final String targetPrefix ) {
        if ( root == null )
            throw new IllegalArgumentException( "The root cannot be null." );
        if ( target == null )
            throw new IllegalArgumentException( "The target cannot be null." );
        if ( targetPrefix == null )
            throw new IllegalArgumentException( "The target prefix cannot be null." );

        return new FileResource( target.getAbsoluteFile() ) {
            @Override
            public String getName() {
                return targetPrefix + File.separator + PathUtils.relativize( root, target );
            }
        };
    }

    public static ResourceCollection mapResources( final ResourceCollection source,
                                                   final String targetPrefix ) {
        if ( source == null )
            throw new IllegalArgumentException( "The source resource collection cannot be null." );
        if ( targetPrefix == null )
            throw new IllegalArgumentException( "The target prefix cannot be null." );

        if ( targetPrefix.length() == 0 )
            return source;

        return new ResourceCollection() {
            @Override
            public Iterator iterator() {
                return new Iterator() {
                    Iterator iter = source.iterator();

                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    @Override
                    public Object next() {
                        return mapFileResource( (FileResource) iter.next(), targetPrefix );
                    }

                    @Override
                    public void remove() {
                        iter.remove();
                    }
                };
            }

            @Override
            public int size() {
                return source.size();
            }

            @Override
            public boolean isFilesystemOnly() {
                return source.isFilesystemOnly();
            }
        };
    }

    public void copy( final ResourceCollection source, final File to ) throws BuildException {
        if ( source == null )
            throw new IllegalArgumentException( "The source resource collection cannot be null." );
        if ( to == null )
            throw new IllegalArgumentException( "The target direcotry cannot be null." );
        if ( to.exists() && !to.isDirectory() )
            throw new BuildException( "Target path \"" + to + "\" already exists but is not a directory." );

        final Copy copy = (Copy) this.project.createTask( "copy" );
        copy.add( source );
        copy.setTodir( to );
        copy.setOverwrite( true );
        copy.perform();
    }

    public void move( final ResourceCollection source, final File to ) throws BuildException {
        if ( source == null )
            throw new IllegalArgumentException( "The source resource collection cannot be null." );
        if ( to == null )
            throw new IllegalArgumentException( "The target direcotry cannot be null." );
        if ( to.exists() && !to.isDirectory() )
            throw new BuildException( "Target path \"" + to + "\" already exists but is not a directory." );

        final Move move = (Move) this.project.createTask( "move" );
        move.add( source );
        move.setTodir( to );
        move.setOverwrite( true );
        move.perform();
    }
    
    public void compile( final Path source, final File to ) throws BuildException {
        if ( source == null )
            throw new IllegalArgumentException( "The source resource collection cannot be null." );
        if ( to == null )
            throw new IllegalArgumentException( "The target direcotry cannot be null." );
        if ( to.exists() && !to.isDirectory() )
            throw new BuildException( "Target path \"" + to + "\" already exists but is not a directory." );

        final Javac javac = (Javac) this.project.createTask( "javac" );
        javac.setSrcdir( source );
        javac.setDestdir( to );
        javac.perform();
    }

    public void compile( final File source, final File to ) throws BuildException {
        if ( source == null )
            throw new IllegalArgumentException( "The source resource collection cannot be null." );
        if ( !source.exists() && !to.isDirectory() )
            throw new BuildException( "Target path \"" + to + "\" already exists but is not a directory." );
        if ( to == null )
            throw new IllegalArgumentException( "The target direcotry cannot be null." );
        if ( to.exists() && !to.isDirectory() )
            throw new BuildException( "Target path \"" + to + "\" already exists but is not a directory." );

        final FileSet fs = (FileSet) this.project.createDataType( "fileset" );
        fs.setDir( source );
        fs.setIncludes( "**/*" );
        final Path path = (Path) this.project.createDataType( "path" );
        compile( path, to );
    }

    public Path getModulePath( final ModuleResolver module, final boolean includeSources,
                                  final boolean includeResources ) throws ResolutionException {
        final Path path = (Path) this.project.createDataType( "path" );
        for ( final String source : module.getModule().getSourceUrls() )
            path.add( resolveModuleSourceRoot( module, source, includeSources, includeResources ) );
        return path;
    }

    public ResourceCollection resolveModuleSourceRoot( final ModuleResolver module, final String rootUrl,
                                                        final boolean includeSources, final boolean includeResources )
            throws ResolutionException {
        // Verify this is an actual source directory
        if ( !module.getModule().getSourceUrls().contains( rootUrl ) )
            throw new ResolutionException( "The web facet specifies source URL \"" + rootUrl +
                    "\", but it is not part of the module." );

        final FileSet fs = (FileSet) this.project.createDataType( "fileset" );
        fs.setDir( module.resolveUriFile( rootUrl ) );
        if ( includeSources )
            fs.setIncludes( "**/*.java" );
        if ( includeResources )
            fs.appendIncludes( generateResourceIncludes( module.getProjectResolver().getProject() ) );
        return fs;
    }
}
