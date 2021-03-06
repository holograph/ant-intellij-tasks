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

public class ResolveModuleClasspathTask extends ModuleTaskBase {
    protected String pathId;
    protected SourceFilter filter = SourceFilter.source;

    public void setPathId( final String pathId ) {
        this.pathId = pathId;
    }

    public SourceFilter getFilter() {
        return this.filter;
    }

    public void setFilter( final SourceFilter filter ) {
        if ( filter == null )
            throw new IllegalArgumentException( "The source filter cannot be null." );
        this.filter = filter;
    }

    @Override
    public void executeTask() throws BuildException {
        if ( this.pathId == null )
            throw new BuildException( "Target path ID (attribute 'pathId') not specified." );

        logVerbose( "Resolving classpath for module '%s', filter=%s", module().getName(), this.filter );
        getProject().addReference( this.pathId, ant().buildModuleClasspath( resolver(), this.filter ) );
    }
}
