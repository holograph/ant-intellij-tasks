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

import org.w3c.dom.Node;

import java.util.*;

public abstract class PackageFacetBase extends Facet {
    private String explodedUrl;
    private String targetUrl;
    private final PackagingContainer packaging;

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
        this.packaging = new PackagingContainer( facetNode, "configuration/packaging" );
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
                    "Module " + getShortName() + " has exploded target enabled but no URL is specified." );
        this.targetUrl = jarEnabled ? extract( facetNode, String.format( path, "JAR_URL" ), message ) : null;
        if ( jarEnabled && this.targetUrl == null )
            throw new ParseException(
                    "Module " + getShortName() + " has exploded target enabled but no URL is specified." );
    }

    protected abstract String getShortName();

    public PackagingContainer getPackagingContainer() {
        return this.packaging;
    }

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

        @Override
        public String toString() {
            return "root[url='" + this.url + "', target='" + this.targetUri + "']";
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
}
