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

/**
 * Provides utility methods to aid Ant scripting.
 */
public class AntUtils {
    /**
     * The Ant project instance.
     */
    private final org.apache.tools.ant.Project project;

    /**
     * Creates and returns a new instance of {@link AntUtils}.
     *
     * @param project The Ant project instance.
     * @throws IllegalArgumentException The project cannot be null.
     */
    public AntUtils( final org.apache.tools.ant.Project project ) throws IllegalArgumentException {
        if ( project == null )
            throw new IllegalArgumentException( "The project cannot be null." );
        this.project = project;
    }

    /**
     * Generates a set of resource inclusion pattern for the specified project.
     * <p/>
     * These patterns follow the Ant pattern format, e.g. <code>**&#x2f;*.xml</code>.
     *
     * @param project The project for which to generate resource inclusion patterns.
     * @return An array of reosurce inclusion patterns for the specified project.
     * @throws IllegalArgumentException The project cannot be null.
     */
    static String[] generateResourceIncludes( final Project project ) throws IllegalArgumentException {
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

    /**
     * Takes a URI and returns it without the preceding slash (if present).
     *
     * @param uri The URI to process. If {@literal null} is specified, this method returns {@literal null}.
     * @return The specified URI with not preceding slash, or {@literal null} if no URI is specified.
     */
    protected static String stripPreceedingSlash( final String uri ) {
        return uri != null ? ( uri.startsWith( "/" ) ? uri.substring( 1 ) : uri ) : null;
    }

    /**
     * An ordered dcontainer for Ant {@link org.apache.tools.ant.types.Resource file resources}.
     */
    public static class ResourceContainer implements ResourceCollection {
        /**
         * The list of reosurces.
         */
        protected final Collection<Resource> resources = new ArrayList<Resource>();

        @Override
        public Iterator iterator() {
            return this.resources.iterator();
        }

        @Override
        public int size() {
            return this.resources.size();
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

        /**
         * Adds a resource to the container.
         *
         * @param resource The resource to add to the container. {@literal null}s are ignored.
         */
        public void add( final Resource resource ) {
            if ( resource != null )
                this.resources.add( resource );
        }
    }

    /**
     * Copies the specified resources to the target directory. If the target directory does not already exist, it is
     * created.
     *
     * @param source The resources to copy.
     * @param to     The target directory.
     * @throws BuildException           An error has occured during the copy.
     * @throws IllegalArgumentException <ul><li>The source resource collection cannot be null.</li><li>The target
     *                                  directory cannot be null.</li><li>Target path already exists but is not a
     *                                  directory.</li></ul>
     */
    public void copy( final ResourceCollection source, final File to ) throws BuildException, IllegalArgumentException {
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

    /**
     * Moves the specified resources to the target directory. If the target directory does not already exist, it is
     * created.
     *
     * @param source The resources to move.
     * @param to     The target directory.
     * @throws BuildException           An error has occured during the move.
     * @throws IllegalArgumentException <ul><li>The source resource collection cannot be null.</li><li>The target
     *                                  directory cannot be null.</li><li>Target path already exists but is not a
     *                                  directory.</li></ul>
     */
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

    /**
     * Takes a set of source directories and compiles all source files to a target directory.
     *
     * @param sourceDirectories The source directories.
     * @param to                The target directory.
     * @param classpath         The compilation classpath as an Ant {@link Path}, or {@literal null} if the default
     *                          classpath is desired.
     * @throws BuildException           An error has occurred during the compilation.
     * @throws IllegalArgumentException <ul><li>The list of source directories cannot be null.</li><li>The target
     *                                  direcotry cannot be null.</li><li>Target path already exists but is not a
     *                                  directory.</li> <li>The source directory list contains a null
     *                                  directory.</li><li>A source path does not exist.</li><li>A source path exists
     *                                  but is not a directory.</li></ul>
     */
    public void compile( final Iterable<File> sourceDirectories, final File to, final Path classpath )
            throws BuildException, IllegalArgumentException {
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
            if ( !directory.exists() )
                throw new IllegalArgumentException( "Source path \"" + directory + "\" does not exist." );
            else if ( !directory.isDirectory() )
                throw new IllegalArgumentException(
                        "Source path \"" + directory + "\" exists but is not a directory." );
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

    /**
     * Resolves all resources in a module source directory into an Ant resource collection.
     *
     * @param module  The module for which to resolve resources.
     * @param rootUrl The source URL for which to resolve resources. The URL <em>must</em> be one of the source URLs
     *                specified in the module.
     * @return A {@link ResourceCollection} which contains the module's resources.
     * @throws ResolutionException      An error has occurred while resolving the module's resources.
     * @throws IllegalArgumentException The specified source URL is not part of the module.
     */
    public ResourceCollection resolveModuleResources( final ModuleResolver module, final String rootUrl )
            throws ResolutionException, IllegalArgumentException {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );

        // Verify this is an actual source directory
        if ( !module.getModule().getSourceUrls().contains( rootUrl ) )
            throw new IllegalArgumentException(
                    "The specified source URL \"" + rootUrl + "\" is not part of the module." );

        final FileSet fs = (FileSet) this.project.createDataType( "fileset" );
        fs.setDir( module.resolveUriFile( rootUrl ) );
        fs.appendIncludes( generateResourceIncludes( module.getProjectResolver().getProject() ) );
        return fs;
    }

    /**
     * Resolves all of the module's resources into an Ant resource collection.
     *
     * @param module The module for which to resolve resources.
     * @return A {@link ResourceCollection} containing the module's resources.
     * @throws IllegalArgumentException The module cannot be null.
     * @throws ResolutionException      An error has occurred while resolving the module's resources.
     */
    public ResourceCollection resolveModuleResources( final ModuleResolver module )
            throws IllegalArgumentException, ResolutionException {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );

        final Path path = (Path) this.project.createDataType( "path" );
        for ( final String sourceUrl : module.getModule().getSourceUrls() )
            path.add( resolveModuleResources( module, sourceUrl ) );
        return path;
    }

    /**
     * Resolves the module's classpath and generates an Ant path object, which can then be passed on to subsequent build
     * tasks (such as javac).
     *
     * @param module The module for which to build the classpath.
     * @param filter A {@link SourceFilter} which specifies which types of sources should be considered for the
     *               classpath.
     * @return A {@link Path} representing the module's classpath.
     * @throws BuildException           An error has occurred while building the classpath.
     * @throws IllegalArgumentException <ul><li>The module cannot be null.</li><li>The source filter cannot be
     *                                  null.</li></ul>
     */
    public Path buildModuleClasspath( final ModuleResolver module, final SourceFilter filter )
            throws BuildException, IllegalArgumentException {
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
            throw new BuildException(
                    "Cannot resolve module classpath for module \"" + module.getModule().getName() + "\".", e );
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
