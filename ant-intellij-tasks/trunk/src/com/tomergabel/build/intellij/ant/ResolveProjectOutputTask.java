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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.util.UriUtils;

public class ResolveProjectOutputTask extends ProjectTaskBase {
    protected String property;

    public String getProperty() {
        return this.property;
    }

    public void setProperty( final String property ) {
        this.property = property;
    }

    @Override
    protected void executeTask() throws BuildException {
        if ( this.property == null )
            throw new BuildException( "Target property (attribute 'property') not specified." );

        log( "Attempting to resolve output URL '" + project().getOutputUrl() + "'", Project.MSG_VERBOSE );
        final String outputDirectory;
        try {
            outputDirectory = UriUtils.getPath( projectResolver().resolveUriString( project().getOutputUrl() ) );
        } catch ( ResolutionException e ) {
            throw new BuildException( "Failed to resolve project output directory.", e );
        }

        log( "Output URL resolved to '" + outputDirectory + "'", Project.MSG_VERBOSE );
        getProject().setProperty( this.property, outputDirectory );
    }
}
