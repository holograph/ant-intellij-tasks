package com.tomergabel.build.intellij.model;

import com.tomergabel.util.LazyInitializationException;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

public class FacetParsingTests {
    @SuppressWarnings( { "ConstantConditions" } )
    @Test
    public void test_ModuleWithWebFacet_WebFacetSuccessfullyParsed() throws LazyInitializationException {
        final Module module = MockModel.Modules.withWebFacet.get();
        final WebFacet facet = resolve( module.getFacets(), WebFacet.class );
        assertEquals( "Web descriptor URL incorrectly parsed.", "file://$MODULE_DIR$/web/WEB-INF/web.xml",
                facet.getWebDescriptorUrl() );
        assertSetEquality( "Source roots incorrectly parsed.",
                Collections.singleton( new PackageFacetBase.Root( "file://$MODULE_DIR$/src", null ) ),
                facet.getSourceRoots() );
        assertSetEquality( "Web roots incorrectly parsed.",
                Collections.singleton( new PackageFacetBase.Root( "file://$MODULE_DIR$/web", "/" ) ),
                facet.getWebRoots() );
        assertNotNull( "Target URL enable flag incorrectly parsed.", facet.getTargetUrl() );
        assertEquals( "Target URL incorrectly parsed.", "file://$MODULE_DIR$/out/test.war", facet.getTargetUrl() );
        assertNotNull( "Explode enable flag incorrectly parsed.", facet.getExplodedUrl() );
        assertEquals( "Exploded URL incorrectly parsed.", "file://$MODULE_DIR$/out/exploded/test",
                facet.getExplodedUrl() );

        assertSetEquality( "Container elements incorrectly parsed.", new PackagingContainer.ContainerElement[] {
                new PackagingContainer.ContainerElement( new ModuleDependency( "with-web-facet" ), PackagingMethod.COPY,
                        "/WEB-INF/classes" ),
                new PackagingContainer.ContainerElement( new ModuleDependency( "dependee" ), PackagingMethod.COPY,
                        "/WEB-INF/classes" ),
                new PackagingContainer.ContainerElement(
                        new NamedLibraryDependency( "junit", LibraryDependency.Level.PROJECT ), PackagingMethod.COPY,
                        "/WEB-INF/lib" ),
        }, facet.getPackagingContainer().getElements() );
    }

    @SuppressWarnings( { "ConstantConditions" } )
    @Test
    public void test_ModuleWithEjbFacet_EjbFacetSuccessfullyParsed() throws LazyInitializationException {
        final Module module = MockModel.Modules.withEjbFacet.get();
        final EjbFacet facet = resolve( module.getFacets(), EjbFacet.class );
        assertNotNull( "Target URL enable flag incorrectly parsed.", facet.getTargetUrl() );
        assertEquals( "Target URL incorrectly parsed.", "file://$MODULE_DIR$/out/with-ejb-facet.jar", facet.getTargetUrl() );
        assertNull( "Explode enable flag incorrectly parsed.", facet.getExplodedUrl() );

        assertSetEquality( "Source roots incorrectly parsed.",
                Collections.singleton( new PackageFacetBase.Root( "file://$MODULE_DIR$/src", null ) ),
                facet.getEjbRoots() );

        assertSetEquality( "Container elements incorrectly parsed.", new PackagingContainer.ContainerElement[] {
                new PackagingContainer.ContainerElement( new ModuleDependency( "dependee" ), PackagingMethod.JAR_AND_LINK,
                        "/dependee.jar" ),
        }, facet.getPackagingContainer().getElements() );
    }                        

    @SuppressWarnings( { "unchecked" } )
    private <U, T extends U> T resolve( final Collection<U> collection, final Class<T> subclass ) {
        T result = null;
        for ( final U x : collection )
            if ( subclass.isInstance( x ) )
                if ( result != null )
                    fail( subclass.getName() + " parsed more than once." );
                else
                    result = (T) x;
        assertNotNull( subclass.getName() + " not parsed.", result );
        return result;
    }
}