package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.PackagingContainer;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.util.UriUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;

public class PackageModuleJarTask extends PackageTaskBase {
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
        jar.getDestFile().delete();

        // Add package output to the JAR task
        try {
            jar.add( resolvePackagingElements() );
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }

        return jar;
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

    @Override
    protected PackagingContainer resolvePackagingContainer() throws ResolutionException {
        return module().getJarSettings();
    }
}
