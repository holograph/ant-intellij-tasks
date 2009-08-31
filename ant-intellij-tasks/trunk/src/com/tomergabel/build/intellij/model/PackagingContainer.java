package com.tomergabel.build.intellij.model;

import org.w3c.dom.Node;

import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;

public class PackagingContainer extends ParserBase {
    private final Collection<ContainerElement> elements = new HashSet<ContainerElement>();

    public static class ContainerElement {
        private final Dependency dependency;
        private final PackagingMethod method;
        private final String targetUri;

        public Dependency getDependency() {
            return this.dependency;
        }

        public PackagingMethod getMethod() {
            return this.method;
        }

        public String getTargetUri() {
            return this.targetUri;
        }

        public ContainerElement( final Dependency dependency, final PackagingMethod method, final String targetUri ) {
            if ( dependency == null )
                throw new IllegalArgumentException( "The dependency cannot be null." );
            if ( method == null )
                throw new IllegalArgumentException( "The method cannot be null." );

            this.dependency = dependency;
            this.method = method;
            this.targetUri = targetUri;
        }

        @SuppressWarnings( { "RedundantIfStatement" } )
        @Override
        public boolean equals( final Object o ) {
            if ( this == o ) return true;
            if ( o == null || getClass() != o.getClass() ) return false;

            final ContainerElement that = (ContainerElement) o;

            if ( this.dependency != null ? !dependency.equals( that.dependency ) : that.dependency != null )
                return false;
            if ( this.method != that.method ) return false;
            if ( this.targetUri != null ? !targetUri.equals( that.targetUri ) : that.targetUri != null ) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.dependency != null ? dependency.hashCode() : 0;
            result = 31 * result + ( this.method != null ? method.hashCode() : 0 );
            result = 31 * result + ( this.targetUri != null ? targetUri.hashCode() : 0 );
            return result;
        }

        @Override
        public String toString() {
            return "ContainerElement[ dependency='" + this.dependency + "', method=" + this.method + ", targetUri='" +
                    this.targetUri + "' ]";
        }
    }

    public Collection<ContainerElement> getElements() {
        return Collections.unmodifiableCollection( this.elements );
    }

    public PackagingContainer( final Node rootNode, final String prefix ) throws ParseException {
        super();

        if ( rootNode == null )
            throw new IllegalArgumentException( "The container node cannot be null." );

        final Node containerNode =
                prefix == null ? rootNode : extractNode( rootNode, prefix, "Cannot extract container root node" );
        if ( prefix == null )
            throw new ParseException( "Cannot find packaging container root node." );

        for ( final Node container : extractAll( containerNode, "containerElement",
                "Cannot extract container elements" ) ) {
            final String type = extract( container, "@type", "Cannot extract container element type" );
            if ( type == null )
                throw new ParseException( "A container element with no type was specified." );

            // Resolve dependency
            final Dependency dependency;
            if ( "module".equals( type ) ) {
                final String name = extract( container, "@name", "Cannot extract module name" );
                if ( name == null )
                    throw new ParseException( "A module container element with no module name was specified." );
                dependency = new ModuleDependency( name );
            } else if ( "library".equals( type ) ) {
                final String levelString = extract( container, "@level", "Cannot extract library level" );
                if ( levelString == null )
                    throw new ParseException( "A library container element with no level was specified." );
                final LibraryDependency.Level level = LibraryDependency.Level.parse( levelString );

                switch ( level ) {
                    case MODULE:
                        dependency = new ModuleLibraryDependency( new Library( container ) );
                        break;

                    case PROJECT:
                        final String name = extract( container, "@name", "Cannot extract library name" );
                        if ( name == null )
                            throw new ParseException(
                                    "A library container element with no library name was specified." );
                        dependency = new NamedLibraryDependency( name, level );
                        break;

                    default:
                        throw new IllegalStateException( "Unexpected library level '" + level + "'" );

                }
            } else throw new ParseException( "An unknown container element type '" + type + "' was specified" );

            // Parse container element settings
            final PackagingMethod method = PackagingMethod
                    .parse( extract( container, "attribute[@name='method']/@value",
                            "Cannot extract packaging method" ) );
            if ( method == null )
                throw new ParseException( "A container element with no packaging method was specified." );
            final String targetUri = extract( container, "attribute[@name='URI']/@value", "Cannot extract target URI" );
            if ( targetUri == null )
                throw new ParseException( "A container element with no target URI was specified." );
            this.elements.add( new ContainerElement( dependency, method, targetUri ) );
        }
    }
}
