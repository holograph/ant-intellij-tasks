package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.*;
import com.tomergabel.util.CharPrefixTree;
import com.tomergabel.util.UriUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.types.selectors.FileSelector;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

public abstract class PackageTaskBase extends ModuleTaskBase {
    protected ResourceCollection resolvePackagingElements() throws ResolutionException {
        final Path path = new Path( getProject() );
        for ( final PackagingContainer.ContainerElement element : resolvePackagingContainer().getElements() ) {
            final String prefix = AntUtils.stripPreceedingSlash( element.getTargetUri() );
            logVerbose( "Resolving %s, method=%s, prefix=%s...", element, element.getMethod(), prefix );
            switch ( element.getMethod() ) {
                case COPY:
                    resolveDependencyOutput( element.getDependency(), path, prefix );
                    break;

                case JAR:
                    resolveDependencyClasspath( element.getDependency(), path, prefix );
                    break;

                default:
                    throw new ResolutionException(
                            "Unrecognized packaging method \"" + element.getMethod() + "\" specified." );
            }
        }
        return path;
    }

    protected abstract PackagingContainer resolvePackagingContainer() throws ResolutionException;

    private void resolveDependencyClasspath( final Dependency dependency, final Path path, final String prefix )
            throws ResolutionException {
        if ( dependency == null )
            throw new IllegalArgumentException( "The dependency cannot be null." );
        if ( path == null )
            throw new IllegalArgumentException( "The target path cannot be null." );
        if ( prefix == null )
            throw new IllegalArgumentException( "The target prefix cannot be null." );

        final ZipFileSet fs = new ZipFileSet();
        fs.setPrefix( prefix );
        fs.setDir( resolver().resolveModuleOutput() );
        final Collection<String> classpath = dependency.resolveClasspath( resolver() );
        fs.appendIncludes( classpath.toArray( new String[ classpath.size() ] ) );
        path.add( fs );
    }

    protected /*static*/ class CompileOutputSelector implements FileSelector {
        private final Collection<String> filenames = new CharPrefixTree();

        public CompileOutputSelector( final ModuleResolver resolver, final String rootUrl ) throws ResolutionException {
            if ( rootUrl == null )
                throw new IllegalArgumentException( "The source root URL cannot be null." );

            // Verify that the source root is part of our resolver
            if ( !resolver.getModule().getSourceUrls().contains( rootUrl ) )
                throw new ResolutionException(
                        "Source root URL \"" + rootUrl + "\" not found  in module \"" + resolver.getModule().getName() +
                                "\"." );

            // Generate source file set
            final FileSet source = new FileSet();
            source.setDir( UriUtils.getFile( resolver.resolveUriString( rootUrl ) ) );
            source.setIncludes( "**/*.java" );                              // Include Java sources
            if ( resolver.getProjectResolver() != null )                    // Include file resources
                source.appendIncludes(
                        AntUtils.generateResourceIncludes( resolver.getProjectResolver().getProject() ) );

            // Required to work around an NPE in AbstractFileSet:477
            source.setProject( getProject() );

            logVerbose( "Iterating source files and resources under " + source.getDir() );
            final Iterator iter = source.iterator();
            while ( iter.hasNext() ) {
                final String filename = mangle( ( (Resource) iter.next() ).getName() );
                logVerbose( "Adding file %s to selector", filename );
                this.filenames.add( filename );
            }
        }

        private String mangle( final String name ) {
            if ( name.endsWith( ".java" ) )
                return name.substring( 0, name.length() - 4 ) + "class";
            else if ( name.endsWith( ".class" ) && name.indexOf( '$' ) != -1 )
                return name.substring( 0, name.indexOf( '$' ) ) + ".class";
            else
                return name;
        }

        @Override
        public boolean isSelected( final File basedir, final String filename, final File file ) throws BuildException {
            final boolean result = this.filenames.contains( mangle( filename ) );
            logVerbose( "Selecting file %s under %s, result=%b", filename, basedir, result );
            return result;
        }
    }

    private void resolveDependencyOutput( final Dependency dependency, final Path path, final String prefix )
            throws BuildException, ResolutionException {
        if ( dependency == null )
            throw new IllegalArgumentException( "The dependency cannot be null." );
        if ( path == null )
            throw new IllegalArgumentException( "The target path cannot be null." );

        if ( dependency instanceof LibraryDependency )
            // Delegate to classpath resolver (resolved classpath points to the various JARs relative to
            // the module output directory - precisely what we need)
            resolveDependencyClasspath( dependency, path, prefix );
        else if ( dependency instanceof ModuleDependency ) {
            final String moduleName = ( (ModuleDependency) dependency ).name;

            // Resolve dependee
            final ModuleResolver dependee;
            if ( moduleName.equals( module().getName() ) )
                dependee = resolver();
            else {
                assertProjectSpecified();
                dependee = projectResolver().getModuleResolver( moduleName );
            }

            path.add( resolveCompileOutput( dependee, prefix ) );
        } else
            throw new ResolutionException(
                    "Cannot resolve output path for dependency " + dependency + ", type not recognized" );
    }

    protected ResourceCollection resolveCompileOutput( final ModuleResolver resolver, final String prefix )
            throws ResolutionException {
        if ( resolver == null )
            throw new IllegalArgumentException( "The module resolver cannot be null." );

        logVerbose( "Resolving compile output for module " + resolver.getModule().getName() );
        final Path p = new Path( getProject() );
        for ( final String source : resolver.getModule().getSourceUrls() )
            p.add( resolveCompileOutput( resolver, source, prefix ) );
        return p;
    }

    protected /*static*/ ResourceCollection resolveCompileOutput( final ModuleResolver resolver,
                                                              final PackageFacetBase.Root root )
            throws ResolutionException {
        if ( resolver == null )
            throw new IllegalArgumentException( "The module resolver cannot be null." );
        if ( root == null )
            throw new IllegalArgumentException( "The source root cannot be null." );

        return resolveCompileOutput( resolver, root.getUrl(), AntUtils.stripPreceedingSlash( root.getTargetUri() ) );
    }

    private /*static*/ ResourceCollection resolveCompileOutput( final ModuleResolver resolver, final String rootUrl, final String prefix )
            throws ResolutionException {
        if ( resolver == null )
            throw new IllegalArgumentException( "The module resolver cannot be null." );
        if ( rootUrl == null )
            throw new IllegalArgumentException( "The root URL cannot be null." );

        logVerbose( "Resolving compile output for source root %s of module %s...", rootUrl,
                resolver.getModule().getName() );
        final ZipFileSet target = new ZipFileSet();
        target.setDir( resolver.resolveModuleOutput() );
        target.setPrefix( prefix );
        target.appendSelector( new CompileOutputSelector( resolver, rootUrl ) );
        return target;
    }
}
