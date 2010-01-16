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

package com.tomergabel.build.intellij.model;

import com.tomergabel.util.Tuple;
import com.tomergabel.util.UriUtils;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represent a single library in an IntelliJ IDEA project.
 */
public final class Library extends ParserBase {
    /**
     * The library name.
     */
    private final String name;
    /**
     * The class file/JAR URIs for this library.
     */
    private final Collection<String> classes;
    /**
     * The javadoc URIs for this library.
     */
    private final Collection<String> javadoc;
    /**
     * The source URIs for this library.
     */
    private final Collection<String> sources;
    /**
     * The JAR directories specified for this library. Each directory is a tuple of the the JAR directory URI and a
     * boolean flag indicating whether or not the directory should be processed recursively.
     */
    private final Collection<Tuple<String, Boolean>> jarDirectories = new HashSet<Tuple<String, Boolean>>();

    /**
     * Package-only c'tor for testing purposes. Do not use!
     *
     * @param name    The library name.
     * @param classes The library's classpath.
     */
    Library( final String name, final Collection<String> classes ) {
        super();
        this.name = name;
        this.classes = new HashSet<String>( classes );
        this.sources = this.javadoc = Collections.emptySet();
    }

    /**
     * Creates and returns a new instance of {@link Library}.
     *
     * @param libraryNode The XML node containing the library descriptor.
     * @throws ParseException           An error has occurred while parsing the library descriptor.
     * @throws IllegalArgumentException The library node cannot be null.
     */
    public Library( final Node libraryNode ) throws ParseException {
        super();

        if ( libraryNode == null )
            throw new IllegalArgumentException( "The library node cannot be null." );

        // Parse library data
        this.name = extract( libraryNode, "@name", "Cannot extract library name" );
        this.classes = iterateRoots( libraryNode, "CLASSES" );
        this.javadoc = iterateRoots( libraryNode, "JAVADOC" );
        this.sources = iterateRoots( libraryNode, "SOURCES" );

        // Parse JAR directories
        for ( final Node jarDirectory : extractAll( libraryNode, "jarDirectory",
                "Can't extract JAR directory elements" ) ) {
            final String url = extract( jarDirectory, "@url", "Can't extract JAR directory URL attribute" );
            if ( url == null )
                throw new ParseException( "JAR directory specified without URL." );
            final boolean recursive = Boolean.parseBoolean(
                    extract( jarDirectory, "@recursive", "Can't extract JAR directory recursive attribute" ) );
            this.jarDirectories.add( new Tuple<String, Boolean>( url, recursive ) );
        }
    }

    /**
     * Gets the library name.
     *
     * @return The library name, or {@literal null} if the library is unnamed.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the collection of class/JAR URIs. These are raw URIs that should be resolved with one of the {@link
     * PropertyResolver} resolution methods.
     * <p/>
     * The collection returned is read-only (any attempts to modify it will result in an {@link
     * UnsupportedOperationException}.
     */
    public Collection<String> getClasses() {
        return this.classes;
    }

    /**
     * Returns the collection of JavaDocs URIs. These are raw URIs that should be resolved with one of the {@link
     * PropertyResolver} resolution methods.
     * <p/>
     * The collection returned is read-only (any attempts to modify it will result in an {@link
     * UnsupportedOperationException}.
     */
    public Collection<String> getJavadoc() {
        return this.javadoc;
    }

    /**
     * Returns the collection of source directory URIs. These are raw URIs that should be resolved with one of the
     * {@link PropertyResolver} resolution methods.
     * <p/>
     * The collection returned is read-only (any attempts to modify it will result in an {@link
     * UnsupportedOperationException}.
     */
    public Collection<String> getSources() {
        return this.sources;
    }

    /**
     * Resolves the classpath for this library using the specified {@link PropertyResolver resolver}.
     *
     * @param resolver The {@link PropertyResolver} used to resolve the library URIs.
     * @return A collection of resolved classpath entries for this library.
     * @throws IllegalArgumentException The property resolver cannot be null.
     * @throws ResolutionException      An error has occurred while resolving the classpath.
     */
    public Collection<String> resolveClasspath( final PropertyResolver resolver )
            throws IllegalArgumentException, ResolutionException {
        if ( resolver == null )
            throw new IllegalArgumentException( "The property resolver cannot be null." );

        final Set<String> classpath = new HashSet<String>();

        // Resolve class URIs
        for ( final String uri : this.classes )
            classpath.add( UriUtils.getPath( resolver.resolveUriString( uri ) ) );

        // Resolve JAR directories
        for ( final Tuple<String, Boolean> jarDirectory : this.jarDirectories )
            resolveJarDirectory( UriUtils.getFile( resolver.resolveUriString( jarDirectory.left ) ), jarDirectory.right,
                    classpath );

        // Return the resolved classpath
        return Collections.unmodifiableCollection( classpath );
    }

    /**
     * Takes a JAR directory and adds all JARs therein (recursion optional) to the specified classpath container.
     *
     * @param directory The directory to resolve.
     * @param recursive Should the directory be reoslved recursively?
     * @param bag       The classpath container to which to add the resolved JARs
     * @throws IllegalArgumentException <ul><li>The JAR directory cannot be null.</li><li>The classpath container cannot
     *                                  be null.</li></ul>
     * @throws ResolutionException      <ul><li>The directory does not exist.</li><li>The directory parameter does not
     *                                  point to a directory.</li></ul>
     */
    private void resolveJarDirectory( final File directory, final boolean recursive, final Set<String> bag )
            throws IllegalArgumentException, ResolutionException {
        if ( !directory.exists() )
            throw new ResolutionException(
                    "Directory \"" + directory + "\" is referenced by " + this.toString() + " but does not exist." );
        if ( !directory.isDirectory() )
            throw new ResolutionException(
                    "Directory \"" + directory + "\" is referenced by " + this.toString() + " but not a directory." );

        for ( final File file : directory.listFiles( new FileFilter() {
            @Override
            public boolean accept( final File pathname ) {
                return ( recursive && pathname.isDirectory() ) || pathname.getName().toLowerCase().endsWith( ".jar" );
            }
        } ) ) {
            if ( recursive && file.isDirectory() )
                resolveJarDirectory( file, recursive, bag );
            else
                bag.add( file.getAbsolutePath() );
        }
    }

    /**
     * Iterates and returns a collection of library roots of the specified type.
     *
     * @param libraryNode The library XML node.
     * @param rootType    The root type string.
     * @return A collection of the root URIs for the specified type.
     * @throws ParseException           An error has occurred while iterating the specified node.
     * @throws IllegalArgumentException <ul>The library node cannot be null.</li><li>The root type string cannot be
     *                                  null.</li></ul>
     */
    private Collection<String> iterateRoots( final Node libraryNode, final String rootType )
            throws IllegalArgumentException, ParseException {
        if ( libraryNode == null )
            throw new IllegalArgumentException( "The library node cannot be null." );
        if ( rootType == null )
            throw new IllegalArgumentException( "The root type string cannot be null." );

        final Set<String> set = new HashSet<String>();
        for ( final Node root : extractAll( libraryNode, rootType + "/root",
                "Cannot extract " + rootType.toLowerCase() + " root paths for library \"" + this.name + "\"" ) )
            set.add( extract( root, "@url",
                    "Cannot extract " + rootType.toLowerCase() + " root path URL for library \"" + this.name + "\"" ) );
        return Collections.unmodifiableCollection( set );
    }

    @Override
    public String toString() {
        return this.name == null ? "an anonymous library" : "library \"" + this.name + "\"";
    }
}
