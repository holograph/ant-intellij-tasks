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

public final class Library extends ParserBase {
    private final String name;
    private final Collection<String> classes = new HashSet<String>();
    private final Collection<String> javadoc = new HashSet<String>();
    private final Collection<String> sources = new HashSet<String>();
    private final Collection<Tuple<String, Boolean>> jarDirectories = new HashSet<Tuple<String, Boolean>>();

    /**
     * Package-only c'tor for testing purposes.
         * @param name The library name.
         * @param classes The library's classpath.
         */
        Library( final String name, final Collection<String> classes ) {
        super();
        this.name = name;
        this.classes.addAll( classes );
    }

    public Library( final Node libraryNode ) throws ParseException {
        super();

        // Parse library data
        this.name = extract( libraryNode, "@name", "Cannot extract library name" );
        iterateRoots( libraryNode, "CLASSES", this.classes );
        iterateRoots( libraryNode, "JAVADOC", this.javadoc );
        iterateRoots( libraryNode, "SOURCES", this.sources );

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

    public String getName() {
        return this.name;
    }

    public Collection<String> getClasses() {
        return Collections.unmodifiableCollection( this.classes );
    }

    public Collection<String> getJavadoc() {
        return Collections.unmodifiableCollection( this.javadoc );
    }

    public Collection<String> getSources() {
        return Collections.unmodifiableCollection( this.sources );
    }

    public Collection<String> resolveClasspath( final PropertyResolver resolver ) throws ResolutionException {
        final Set<String> classpath = new HashSet<String>();
        for ( final String uri : this.classes )
            classpath.add( UriUtils.getPath( resolver.resolveUriString( uri ) ) );
        for ( final Tuple<String, Boolean> jarDirectory : this.jarDirectories )
            resolveJarDirectory( UriUtils.getFile( resolver.resolveUriString( jarDirectory.left ) ), jarDirectory.right, classpath );
        return Collections.unmodifiableCollection( classpath );
    }

    private void resolveJarDirectory( final File directory, final boolean recursive, final Set<String> bag )
            throws ResolutionException {
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

    private void iterateRoots( final Node libraryNode, final String node,
                               final Collection<String> target )
            throws ParseException {
        for ( final Node root : extractAll( libraryNode, node + "/root",
                "Cannot extract " + node.toLowerCase() + " root paths for library \"" + this.name + "\"" ) )
            target.add( extract( root, "@url",
                    "Cannot extract " + node.toLowerCase() + " root path URL for library \"" + this.name +
                            "\"" ) );
    }

    @Override
    public String toString() {
        return this.name == null ? "an anonymous library" : "library \"" + this.name + "\"";
    }
}
