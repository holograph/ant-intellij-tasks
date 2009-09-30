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

import com.tomergabel.build.intellij.model.ResolutionException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class ResolveModuleOutputTask extends ModuleTaskBase {
    public enum Mode {
        test,
        source
    }

    protected String property;
    protected Mode mode = Mode.source;

    public void setProperty( final String property ) {
        this.property = property;
    }

    public void setMode( final Mode mode ) {
        if ( mode == null )
            throw new IllegalArgumentException( "The mode cannot be null." );

        this.mode = mode;
    }

    public String getProperty() {
        return this.property;
    }

    public Mode getMode() {
        return this.mode;
    }

    @Override
    public void executeTask() throws BuildException {
        if ( this.property == null )
            throw new BuildException( "Target property (attribute 'property') not specified." );

        log( "Attempting to resolve output URL '" +
                ( module().getOutputUrl() != null ? module().getOutputUrl() : "null" ) + "'", Project.MSG_VERBOSE );
        final String outputDirectory;
        try {
            outputDirectory = resolver().resolveModuleOutputPath( this.mode == Mode.test );
        } catch ( ResolutionException e ) {
            throw new BuildException( "Failed to resolve module output directory.", e );
        }

        log( "Output URL resolved to '" + outputDirectory + "'", Project.MSG_VERBOSE );
        getProject().setProperty( this.property, outputDirectory );
    }
}
