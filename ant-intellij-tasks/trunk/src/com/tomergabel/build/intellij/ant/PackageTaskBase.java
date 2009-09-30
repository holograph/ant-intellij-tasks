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

import com.tomergabel.build.intellij.model.*;
import static com.tomergabel.util.CollectionUtils.join;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;
import java.util.*;

public abstract class PackageTaskBase extends ModuleTaskBase {
    protected void packageContainerElements( final File to ) throws BuildException {
        if ( to == null )
            throw new IllegalArgumentException( "The target directory cannot be null." );
        if ( to.exists() && !to.isDirectory() )
            throw new BuildException( "Target path \"" + to + "\" already exists but is not a directory." );

        try {
            // Resolve dependencies
            final Map<Module, PackagingContainer.ContainerElement> modules = new HashMap<Module, PackagingContainer.ContainerElement>();
            final List<PackagingContainer.ContainerElement> libraries = new ArrayList<PackagingContainer.ContainerElement>();

            logVerbose( "Packaging container elements of module \"%s\" to \"%s\"...", module().getName(), to );
            for ( final PackagingContainer.ContainerElement element : resolvePackagingContainer().getElements() )
                if ( element.getDependency() instanceof LibraryDependency )
                    libraries.add( element );
                else if ( element.getDependency() instanceof ModuleDependency ) {
                    final String moduleName = ( (ModuleDependency) element.getDependency() ).name;
                    logVerbose( "Resolving module dependency \"%s\"", moduleName );
                    try {
                        modules.put( projectResolver().getModule( moduleName ), element );
                    } catch ( ResolutionException e ) {
                        logWarn( e, "Module \"%s\" package depends on module \"%s\", " +
                                "which cannot be found in the project.", module().getName(), moduleName );
                    }
                } else throw new BuildException( "Unrecognized dependency type " + element.getDependency() );


            // Resolve the build order for all module dependencies
            logVerbose( "Resolving dependency build order for module \"%s\"...", module().getName() );
            final Collection<Module> buildOrder = projectResolver().resolveModuleBuildOrder( modules.keySet() );
            logDebug( "Resolved build order: %s", join( buildOrder, ResolutionModes.names.mapper ) );

            // Copy libraries
            logVerbose( "Copying libraries to module \"%s\" output directory...", module().getName() );
            for ( final PackagingContainer.ContainerElement element : libraries )
                handleLibraryContainerElement( (LibraryDependency) element.getDependency(), getTarget( to, element ),
                        element.getMethod() );

            // Build modules
            logVerbose( "Building and packaging dependencies for module \"%s\"...", module().getName() );
            for ( final Module module : buildOrder ) {
                final PackagingContainer.ContainerElement element = modules.get( module );
                assert element != null;
                handleModuleContainerElement( projectResolver().getModuleResolver( module ), getTarget( to, element ),
                        element.getMethod() );
            }
        } catch ( ResolutionException e ) {
            throw new BuildException(
                    "Failed to resolve packaging instructions for module \"" + module().getName() + "\"", e );
        }
    }

    private void handleModuleContainerElement( final ModuleResolver dependency, final File target,
                                               final PackagingMethod method )
            throws ResolutionException {
        switch ( method ) {
            case COPY:
                logVerbose( "Packaging dependee module \"%s\" to \"%s\"...", dependency.getModule().getName(), target );
                ant().compile( dependency.resolveUriFiles( dependency.getModule().getSourceUrls() ), target,
                        ant().buildClasspath( dependency, SourceFilter.source ) );
                ant().copy( ant().resolveModuleResources( dependency ), target );
                break;

            case JAR:
                // TODO refactor and implement
                throw new BuildException( "JAR packaging method is not currently supported." );

            default:
                throw new BuildException(
                        "Unrecognized packaging method \"" + method + "\" specified." );
        }
    }

    private File getTarget( final File to, final PackagingContainer.ContainerElement element ) {
        final String prefix = AntUtils.stripPreceedingSlash( element.getTargetUri() );
        logVerbose( "Resolving %s, method=%s", element, element.getMethod(), prefix );
        final File target = new File( to, prefix );
        target.mkdirs();
        return target;
    }

    private void handleLibraryContainerElement( final LibraryDependency library, final File target,
                                                final PackagingMethod method )
            throws ResolutionException {
        switch ( method ) {
            case COPY:
                ant().copy( resolveDependencyClasspath( library ), target );
                break;

            case JAR:
                throw new BuildException( "Cannot apply JAR packging method to libraries." );

            default:
                throw new BuildException(
                        "Unrecognized packaging method \"" + method + "\" specified." );
        }
    }

    protected abstract PackagingContainer resolvePackagingContainer() throws ResolutionException;

    private ResourceCollection resolveDependencyClasspath( final Dependency dependency )
            throws ResolutionException {
        if ( dependency == null )
            throw new IllegalArgumentException( "The dependency cannot be null." );

        logVerbose( "Resolving classpath for dependency " + dependency );
        final Collection<String> classpath = dependency.resolveClasspath( resolver(), true, false );
        final AntUtils.ResourceContainer rc = new AntUtils.ResourceContainer();
        for ( final String entry : classpath ) {
            final File entryFile = new File( entry ).getAbsoluteFile();
            rc.add( new FileResource( entryFile.getParentFile(), entryFile.getName() ) );
        }
        return rc;
    }

    static final File tempDir = new File( System.getProperty( "java.io.tmpdir" ) );

    protected File getTemporaryDirectory( final String buildTypeKey ) throws ResolutionException {
        final String key = buildTypeKey + ":" + module().getName();
        final File target = new File( tempDir, "idea-" + Integer.toHexString( key.hashCode() ) );
        target.mkdirs();
        return target;
    }
}
