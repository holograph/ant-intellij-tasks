package com.tomergabel.build.intellij.model;

import org.w3c.dom.Node;

import java.util.*;

public abstract class PackageFacetBase extends Facet {

    private String explodedUrl;
    private String targetUrl;
    private final Collection<ContainerElement> elements = new HashSet<ContainerElement>();

    public Collection<ContainerElement> getElements() {
        return this.elements;
    }

    public String getExplodedUrl() {
        return this.explodedUrl;
    }

    public String getTargetUrl() {
        return this.targetUrl;
    }

    public boolean isExplodeEnabled() {
        return this.explodedUrl != null;
    }

    public boolean isTargetEnabled() {
        return this.targetUrl != null;
    }

    public PackageFacetBase( final Node facetNode ) throws IllegalArgumentException, ParseException {
        super( facetNode );

        parseBuildParameters( facetNode );
        parsePackagingOptions( facetNode );


    }

    private void parsePackagingOptions( final Node facetNode ) throws ParseException {
        for ( final Node container : extractAll( facetNode, "packaging/containerElement",
                "Cannot extract container elements" ) ) {
            final String type = extract( container, "@type", "Cannot extract container element type" );
            if ( type == null )
                throw new ParseException( getShortName() + " specifies a container element with no type." );

            // Resolve dependency
            final Dependency dependency;
            if ( "module".equals( type ) ) {
                final String name = extract( container, "@name", "Cannot extract module name" );
                if ( name == null )
                    throw new ParseException(
                            getShortName() + " specifies a module container element with no module name." );
                dependency = new ModuleDependency( name );
            } else if ( "library".equals( type ) ) {
                final String name = extract( container, "@name", "Cannot extract library name" );
                if ( name == null )
                    throw new ParseException(
                            getShortName() + " specifies a library container element with no library name." );
                final String levelString = extract( container, "@name", "Cannot extract library level" );
                if ( levelString == null )
                    throw new ParseException(
                            getShortName() + " specifies a library container element with no level." );
                final LibraryDependency.Level level = LibraryDependency.Level.parse( levelString );
                dependency = new LibraryDependency() {
                    @Override
                    public Level getLevel() {
                        return level;
                    }

                    @Override
                    public Library resolveLibrary( final ModuleResolver resolver ) throws ResolutionException {
                        if ( resolver.getProjectResolver() == null )
                            throw new ResolutionException(
                                    "Cannot resolve library '" + name + "', project not defined." );
                        final Library library = resolver.getProjectResolver().getProject().getLibraries().get( name );
                        if ( library == null )
                            throw new ResolutionException( "Library '" + name + "' not defined in project." );
                        return library;
                    }
                };
            } else throw new ParseException(
                    getShortName() + "specifies an unknown container element type '" + type + "'" );

            // Parse container element settings
            final PackagingMethod method = PackagingMethod
                    .parse( extract( container, "attribute[@name='method']/@value",
                            "Cannot extract packaging method" ) );
            if ( method == null )
                throw new ParseException( getShortName() + " specifies a container element with no packaging method." );
            final String targetUri = extract( container, "attribute[@name='uri']/@value", "Cannot extract target URI" );
            if ( targetUri == null )
                throw new ParseException( getShortName() + " specifies a container element with no target URI." );
            this.elements.add( new ContainerElement( dependency, method, targetUri ) );
        }
    }

    private void parseBuildParameters( final Node facetNode ) throws ParseException {
        final String message = "Cannot extract build option.";
        final String path = "configuration/building/setting[@name='%s']/@value";
        final boolean explodeEnabled = "true"
                .equals( extract( facetNode, String.format( path, "EXPLODED_ENABLED" ), message ) );
        final boolean jarEnabled = "true"
                .equals( extract( facetNode, String.format( path, "JAR_ENABLED" ), message ) );
        this.explodedUrl = explodeEnabled ? extract( facetNode, String.format( path, "EXPLODED_URL" ), message ) : null;
        if ( explodeEnabled && this.explodedUrl == null )
            throw new ParseException(
                    "Module " + getShortName() + " have exploded target enabled but no URL is specified." );
        this.targetUrl = jarEnabled ? extract( facetNode, String.format( path, "JAR_URL" ), message ) : null;
        if ( jarEnabled && this.targetUrl == null )
            throw new ParseException(
                    "Module " + getShortName() + " have exploded target enabled but no URL is specified." );
    }

    protected abstract String getShortName();

    public static class Root {
        private final String url;
        private final String targetUri;

        public Root( final String url, final String targetUri ) {
            if ( url == null )
                throw new IllegalArgumentException( "The web root URL cannot be null." );
            this.url = url;
            this.targetUri = targetUri;
        }

        public String getUrl() {
            return this.url;
        }

        public String getTargetUri() {
            return this.targetUri;
        }

        @SuppressWarnings( { "RedundantIfStatement" } )
        @Override
        public boolean equals( final Object o ) {
            if ( this == o ) return true;
            if ( o == null || getClass() != o.getClass() ) return false;

            final Root webRoot = (Root) o;

            if ( this.targetUri != null ? !targetUri.equals( webRoot.targetUri ) : webRoot.targetUri != null )
                return false;
            if ( this.url != null ? !url.equals( webRoot.url ) : webRoot.url != null ) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.url != null ? url.hashCode() : 0;
            result = 31 * result + ( this.targetUri != null ? targetUri.hashCode() : 0 );
            return result;
        }
    }

    protected static Collection<Root> parseRoots( final Node facetNode, final String containerName )
            throws ParseException {
        final Collection<Root> roots = new HashSet<Root>();
        for ( final Node root : extractAll( facetNode, "configuration/" + containerName + "/root",
                "Cannot extract web root list." ) ) {
            final String url = extract( root, "@url", "Cannot extract URL." );
            if ( url == null )
                throw new ParseException( "Module " + containerName + " settings specify a root with no URL." );
            final String targetUri = extract( root, "@relative", "Cannot extract target URI." );
            roots.add( new Root( url, targetUri ) );
        }
        return Collections.unmodifiableCollection( roots );
    }

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
    }
}
