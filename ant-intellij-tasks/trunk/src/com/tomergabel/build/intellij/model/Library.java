package com.tomergabel.build.intellij.model;

import org.w3c.dom.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public final class Library extends ParserBase {
    private final String name;
    private final Collection<String> classes = new HashSet<String>();
    private final Collection<String> javadoc = new HashSet<String>();
    private final Collection<String> sources = new HashSet<String>();

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

    private void iterateRoots( final Node libraryNode, final String node,
                               final Collection<String> target )
            throws ParseException {
        for ( final Node root : extractAll( libraryNode, node + "/root",
                "Cannot extract " + node.toLowerCase() + " root paths for library \"" + this.name + "\"" ) )
            target.add( extract( root, "@url",
                    "Cannot extract " + node.toLowerCase() + " root path URL for library \"" + this.name +
                            "\"" ) );
    }
}
