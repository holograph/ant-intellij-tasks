/*
	Copyright 2009 Tomer Gabel <tomer@tomergabel.com>

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


	ant-intellij-tasks project (http://code.google.com/p/ant-intellij-tasks/)

	$Id$
*/

package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.ModuleResolver;
import com.tomergabel.build.intellij.model.Project;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.util.CollectionUtils;
import com.tomergabel.util.Predicate;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Move;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;

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

//    public static class SingletonResource implements ResourceCollection {
//        private final Resource resource;
//
//        public SingletonResource( final Resource resource ) {
//            if ( resource == null )
//                throw new IllegalArgumentException( "The resource cannot be null." );
//            this.resource = resource;
//        }
//
//        @Override
//        public Iterator iterator() {
//            return Collections.singleton( this.resource ).iterator();
//        }
//
//        @Override
//        public int size() {
//            return 1;
//        }
//
//        @Override
//        public boolean isFilesystemOnly() {
//            return resource.isFilesystemOnly();
//        }
//
//        public Resource getResource() {
//            return this.resource;
//        }
//    }
//
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
//
//    public static Resource mapFileResource( final FileResource resource, final String targetPrefix ) {
//        if ( resource == null )
//            throw new IllegalArgumentException( "The file resource cannot be null." );
//        if ( targetPrefix == null )
//            throw new IllegalArgumentException( "The target prefix cannot be null." );
//
//        return new FileResource( resource.getFile() ) {
//            @Override
//            public String getName() {
//                return targetPrefix + File.separator +
//                        PathUtils.relativize( resource.getBaseDir(), resource.getFile() );
//            }
//        };
//    }
//
//    public static Resource mapFileResource( final File file, final String targetLocation ) {
//        if ( file == null )
//            throw new IllegalArgumentException( "The file cannot be null." );
//        if ( targetLocation == null )
//            throw new IllegalArgumentException( "The target location cannot be null." );
//        if ( !file.isAbsolute() )
//            throw new IllegalArgumentException( "The file path must be absolute." );
//
//        return new FileResource( file.getAbsoluteFile() ) {
//            @Override
//            public String getName() {
//                return targetLocation;
//            }
//        };
//    }
//
//    public static Resource mapFileResource( final File root, final File target, final String targetPrefix ) {
//        if ( root == null )
//            throw new IllegalArgumentException( "The root cannot be null." );
//        if ( target == null )
//            throw new IllegalArgumentException( "The target cannot be null." );
//        if ( targetPrefix == null )
//            throw new IllegalArgumentException( "The target prefix cannot be null." );
//
//        return new FileResource( target.getAbsoluteFile() ) {
//            @Override
//            public String getName() {
//                return targetPrefix + File.separator + PathUtils.relativize( root, target );
//            }
//        };
//    }
//
//    public static ResourceCollection mapResources( final ResourceCollection source,
//                                                   final String targetPrefix ) {
//        if ( source == null )
//            throw new IllegalArgumentException( "The source resource collection cannot be null." );
//        if ( targetPrefix == null )
//            throw new IllegalArgumentException( "The target prefix cannot be null." );
//
//        if ( targetPrefix.length() == 0 )
//            return source;
//
//        return new ResourceCollection() {
//            @Override
//            public Iterator iterator() {
//                return new Iterator() {
//                    Iterator iter = source.iterator();
//
//                    @Override
//                    public boolean hasNext() {
//                        return iter.hasNext();
//                    }
//
//                    @Override
//                    public Object next() {
//                        return mapFileResource( (FileResource) iter.next(), targetPrefix );
//                    }
//
//                    @Override
//                    public void remove() {
//                        iter.remove();
//                    }
//                };
//            }
//
//            @Override
//            public int size() {
//                return source.size();
//            }
//
//            @Override
//            public boolean isFilesystemOnly() {
//                return source.isFilesystemOnly();
//            }
//        };
//    }

    public void copy( final ResourceCollection source, final File to ) throws BuildException {
        if ( source == null )
            throw new IllegalArgumentException( "The source resource collection cannot be null." );
        if ( to == null )
            throw new IllegalArgumentException( "The target direcotry cannot be null." );
        if ( to.exists() && !to.isDirectory() )
            throw new BuildException( "Target path \"" + to + "\" already exists but is not a directory." );
        to.mkdirs();

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
        to.mkdirs();

        final Move move = (Move) this.project.createTask( "move" );
        move.add( source );
        move.setTodir( to );
        move.setOverwrite( true );
        move.perform();
    }

    public void compile( final File source, final File to, final Path classpath ) throws BuildException {
        if ( source == null )
            throw new IllegalArgumentException( "The source directory cannot be null." );
        compile( Collections.singleton( source ), to, classpath );
    }

    public void compile( final Iterable<File> sourceDirectories, final File to, final Path classpath ) throws BuildException {
        if ( sourceDirectories == null )
            throw new IllegalArgumentException( "The list of source directories cannot be null." );
        if ( to == null )
            throw new IllegalArgumentException( "The target direcotry cannot be null." );
        if ( to.exists() && !to.isDirectory() )
            throw new BuildException( "Target path \"" + to + "\" already exists but is not a directory." );
        to.mkdirs();

        final Javac javac = (Javac) this.project.createTask( "javac" );
        for ( final File directory : sourceDirectories ) {
            if ( directory == null )
                throw new IllegalArgumentException( "The source directory list contains a null directory." );
            if ( !directory.exists() && !to.isDirectory() )
                throw new BuildException( "Target path \"" + to + "\" already exists but is not a directory." );
            final Path path = (Path) this.project.createDataType( "path" );
            path.setLocation( directory );
            javac.setSrcdir( path );
        }
        javac.setIncludes( "**/*.java" );
        javac.setDestdir( to );
        if ( classpath != null )
            javac.setClasspath( classpath );
        javac.perform();
    }

    public ResourceCollection resolveModuleResources( final ModuleResolver module, final String rootUrl )
            throws ResolutionException {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );

        // Verify this is an actual source directory
        if ( !module.getModule().getSourceUrls().contains( rootUrl ) )
            throw new ResolutionException( "The web facet specifies source URL \"" + rootUrl +
                    "\", but it is not part of the module." );

        final FileSet fs = (FileSet) this.project.createDataType( "fileset" );
        fs.setDir( module.resolveUriFile( rootUrl ) );
        fs.appendIncludes( generateResourceIncludes( module.getProjectResolver().getProject() ) );
        return fs;
    }

    public ResourceCollection resolveModuleResources( final ModuleResolver module )
            throws ResolutionException {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );

        final Path path = (Path) this.project.createDataType( "path" );
        for ( final String sourceUrl : module.getModule().getSourceUrls() )
            path.add( resolveModuleResources( module, sourceUrl ) );
        return path;
    }

    public Path buildClasspath( final ModuleResolver module, final SourceFilter filter ) throws BuildException {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );
        if ( filter == null )
            throw new IllegalArgumentException( "The source filter cannot be null." );

        // Resolve classpath
        final Collection<String> resolved;
        try {
            resolved = module.resolveModuleClasspath( filter == SourceFilter.source || filter == SourceFilter.both,
                    filter == SourceFilter.test || filter == SourceFilter.both );
        } catch ( ResolutionException e ) {
            throw new BuildException( "Cannot resolve module classpath for module \"" + module.getModule().getName() + "\".", e );
        }

        // Create path object and add reference by name
        final Path classpath = (Path) this.project.createDataType( "path" );
        for ( final String path : resolved ) {
            final Path entry = (Path) this.project.createDataType( "path" );
            entry.setPath( path );
            classpath.add( entry );
        }
        return classpath;
    }

}
