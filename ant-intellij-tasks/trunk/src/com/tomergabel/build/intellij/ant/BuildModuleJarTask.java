package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.ModuleResolver;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.util.UriUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.ZipFileSet;

public class BuildModuleJarTask extends ModuleTaskBase {
    @Override
    protected void executeTask() throws BuildException {
        // Set up JAR task
        createJarTask().execute();
    }

    protected Jar createJarTask() throws BuildException {
        if ( !module().isBuildJar() )
            throw new BuildException( "Module \"" + module().getName() + "\" does not specify JAR output settings." );

        final Module.JarSettings settings = module().getJarSettings();
        assert null != settings;

        final Jar jar = instantiateJarTask();
        if ( jar == null )
            throw new BuildException( "Cannot spawn JAR task for module \"" + module().getName() + "\"" );

        // Generate and apply manifest
        try {
            jar.addConfiguredManifest( generateManifest( settings.getMainClass() ) );
        } catch ( ManifestException e ) {
            throw new BuildException( "Failed to generate JAR manifest.", e );
        }

        // Set destination file
        try {
            jar.setDestFile( UriUtils.getFile( resolver().resolveUriString( settings.getJarUrl() ) ) );
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }

        // Iterate outputs and add to the JAR task
        for ( final Module.JarModuleOutput output : settings.getModuleOutputs() ) {
            // Resolve module
            final ModuleResolver dependency;
            if ( output.getModuleName().equals( module().getName() ) )
                dependency = resolver();
            else {
                assertProjectSpecified();
                try {
                    dependency = projectResolver().getModuleResolver( output.getModuleName() );
                } catch ( ResolutionException e ) {
                    throw new BuildException( e );
                }
            }

            // TODO add support for other packaging types
            if ( output.getPackaging() != Module.JarModuleOutput.Packaging.COPY )
                throw new BuildException( "Module \"" + output.getModuleName() + "\" specifies unsupported " +
                        "packaging mode " + output.getPackaging().toString() );

            // Generate fileset
            final ZipFileSet fileset = new ZipFileSet();
            try {
                fileset.setDir( dependency.resolveModuleOutput() );
                fileset.setPrefix( stripPreceedingSlash( output.getTargetUri().toString() ) );
            } catch ( ResolutionException e ) {
                throw new BuildException( e );
            }
            jar.add( fileset );
        }

        return jar;
    }

    protected static String stripPreceedingSlash( final String uri ) {
        return uri.startsWith( "/" ) ? uri.substring( 1 ) : uri;
    }

    protected Jar instantiateJarTask() {
        return (Jar) getProject().createTask( "jar" );
    }

    protected Manifest generateManifest( final String mainClass ) throws ManifestException {
        final Manifest manifest = new Manifest();
        manifest.addConfiguredAttribute( new Manifest.Attribute( "Manifest-Version", "1.0" ) );
        manifest.addConfiguredAttribute( new Manifest.Attribute( "Created-By", "ant-intellij-tasks" ) );
        if ( mainClass != null )
            manifest.addConfiguredAttribute( new Manifest.Attribute( "Main-Class", mainClass ) );
        return manifest;
    }
}
