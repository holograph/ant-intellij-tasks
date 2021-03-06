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
    protected File targetFile;

    public File getTargetFile() {
        return this.targetFile;
    }

    public void setTargetFile( final File targetFile ) {
        this.targetFile = targetFile;
    }

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
            final File target = this.targetFile != null ? this.targetFile
                    : UriUtils.getFile( resolver().resolveUriString( settings.getJarUrl() ) );
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
