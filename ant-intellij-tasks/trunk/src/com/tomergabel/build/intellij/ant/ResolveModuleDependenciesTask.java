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
import com.tomergabel.build.intellij.model.ResolutionException;
import static com.tomergabel.util.CollectionUtils.join;
import static com.tomergabel.util.CollectionUtils.map;
import org.apache.tools.ant.BuildException;

import java.util.Collection;

public class ResolveModuleDependenciesTask extends ModuleTaskBase {
    protected static final String LIST_SEPARATOR = ",";

    protected String property;
    protected ResolutionModes mode = ResolutionModes.names;

    public ResolutionModes getMode() {
        return this.mode;
    }

    public void setMode( final ResolutionModes mode ) throws IllegalArgumentException {
        if ( mode == null )
            throw new IllegalArgumentException( "Mode value cannot be null." );

        this.mode = mode;
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty( final String property ) {
        this.property = property;
    }

    @Override
    public void executeTask() throws BuildException {
        if ( this.property == null )
            throw new BuildException( "Target property (attribute 'property') not specified." );

        // Resolve module dependencies
        final Collection<Module> modules;
        try {
            modules = resolver().resolveModuleDependencies();
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }

        // Set the target property
        getProject().setProperty( this.property, join( map( modules, this.mode.mapper ), LIST_SEPARATOR ) );
    }
}
