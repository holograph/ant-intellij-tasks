package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;
import java.util.Collection;

public abstract class PackageTaskBase extends ModuleTaskBase {
    protected void packageContainerElements( final File to ) throws BuildException {
        if ( to == null )
            throw new IllegalArgumentException( "The target directory cannot be null." );
        if ( to.exists() && !to.isDirectory() )
            throw new BuildException( "Target path \"" + to + "\" already exists but is not a directory." );

        try {
            for ( final PackagingContainer.ContainerElement element : resolvePackagingContainer().getElements() ) {
                final String prefix = AntUtils.stripPreceedingSlash( element.getTargetUri() );
                logVerbose( "Resolving %s, method=%s, prefix=%s...", element, element.getMethod(), prefix );
                final File target = new File( to, prefix );
                switch ( element.getMethod() ) {
                    case COPY:
                        if ( element.getDependency() instanceof ModuleDependency ) {
                            final ModuleDependency module = (ModuleDependency) element.getDependency();
                            final ModuleResolver dependency = projectResolver().getModuleResolver( module.name );
                            final Path sources = ant().getModulePath( dependency, true, false );
                            ant().compile( sources, target );
                            final Path resources = ant().getModulePath( dependency, true, false );
                            ant().copy( resources, target );
                        } else if ( element.getDependency() instanceof LibraryDependency ) {
                            ant().copy( resolveDependencyClasspath( element.getDependency() ), target );
                        } else throw new BuildException( "Unrecognized dependency type " + element.getDependency() );
                        break;

                    case JAR:
                        if ( element.getDependency() instanceof ModuleDependency ) {
                            // TODO refactor and implement
                            throw new BuildException( "JAR packaging method is not currently supported." );
                        } else if ( element.getDependency() instanceof LibraryDependency ) {
                            throw new BuildException( "Cannot apply JAR packging method to libraries." );
                        } else throw new BuildException( "Unrecognized dependency type " + element.getDependency() );

                    default:
                        throw new BuildException(
                                "Unrecognized packaging method \"" + element.getMethod() + "\" specified." );
                }
            }
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }
    }

    protected abstract PackagingContainer resolvePackagingContainer() throws ResolutionException;

    private ResourceCollection resolveDependencyClasspath( final Dependency dependency )
            throws ResolutionException {
        if ( dependency == null )
            throw new IllegalArgumentException( "The dependency cannot be null." );

        logVerbose( "Resolving classpath for dependency " + dependency );
        final Collection<String> classpath = dependency.resolveClasspath( resolver() );
        final AntUtils.ResourceContainer rc = new AntUtils.ResourceContainer();
        for ( final String entry : classpath ) {
            final File entryFile = new File( entry ).getAbsoluteFile();
            rc.add( new FileResource( entryFile.getParentFile(), entryFile.getName() ) );
        }
        return rc;
    }
}
