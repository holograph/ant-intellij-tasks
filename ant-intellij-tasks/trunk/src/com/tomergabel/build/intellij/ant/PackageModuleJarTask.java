package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.PackagingContainer;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.util.UriUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.FileSet;

import java.io.File;

public class PackageModuleJarTask extends PackageTaskBase {
    @Override
    protected void executeTask() throws BuildException {
        if ( !module().isBuildJar() )
            throw new BuildException( "Module \"" + module().getName() + "\" does not specify JAR output settings." );

        final Module.JarSettings settings = module().getJarSettings();
        assert null != settings;

        // Generate temporary directory
        final File tempDir;
        try {
            tempDir = getTemporaryDirectory( "jar" );
        } catch ( ResolutionException e ) {
            throw new BuildException( "Cannot create temporary target directory.", e );
        }

        // Build target
        packageContainerElements( tempDir );

        // Generate and apply manifest
        final Jar jar = (Jar) getProject().createTask( "jar" );
        try {
            jar.addConfiguredManifest( generateManifest( settings.getMainClass() ) );
        } catch ( ManifestException e ) {
            throw new BuildException( "Failed to generate JAR manifest.", e );
        }

        // Set destination file
        try {
            final File target = UriUtils.getFile( resolver().resolveUriString( settings.getJarUrl() ) );
            target.getParentFile().mkdirs();     // Ensure target directory exists
            jar.setDestFile( target );
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }
        jar.getDestFile().delete();

        // Add package output to the JAR task
        final FileSet fs = (FileSet) getProject().createDataType( "fileset" );
        fs.setDir( tempDir );
        fs.setIncludes( "**/*" );
        jar.add( fs );
        jar.perform();

        // Delete temporary directory
        logVerbose( "Deleting temporary directory " + tempDir );
        final Delete delete = (Delete) getProject().createTask( "delete" );
        delete.setDir( tempDir );
        delete.perform();
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
