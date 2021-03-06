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

import com.tomergabel.build.intellij.model.IntelliJParserBase;
import com.tomergabel.build.intellij.model.ParseException;
import com.tomergabel.build.intellij.model.Project;
import com.tomergabel.build.intellij.model.ProjectResolver;
import com.tomergabel.build.intellij.ant.prototype.ProjectReceiver;
import com.tomergabel.util.Lazy;
import com.tomergabel.util.LazyInitializationException;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * A base task which provides a convenient way for subtasks to access a project file.
 * <p/>
 * This task allows a project to be specified in one of three ways: <ul> <li>Project file location (i.e. a file system
 * path to an <tt>.ipr</tt> file) via {@link #setProjectFile(java.io.File)} or via the <tt>projectFile</tt> Ant
 * property.</li> <li>Project descriptor URI (i.e. a URI which points to an <tt>.ipr</tt> file resource) via {@link
 * #setProjectDescriptor(java.net.URI)} or via the <tt>projectDescriptor</tt> Ant property.</li> <li>Project instance
 * via the {@link #setProject(com.tomergabel.build.intellij.model.Project)}. This API is intended for tests or for
 * automating this task via code, and cannot be directly accessed from within Ant.</li></ul>
 * <p/>
 * Inheritors can access the project instance via {@link #project()} or {@link #projectResolver()} TODO complete
 * documentation
 */
public abstract class ProjectTaskBase extends TaskBase implements ProjectReceiver {
    private Lazy<Project> project = Lazy.from( null );

    private final Lazy<ProjectResolver> projectResolver = new Lazy<ProjectResolver>() {
        @Override
        public ProjectResolver call() throws Exception {
            final Project project = ProjectTaskBase.this.project.get();
            return project != null ? new ProjectResolver( project ) : null;
        }
    };

    // Ant-facing properties

    @Override
    public void setProjectFile( final File projectFile ) {
        assertNotExecuted();
        setProjectDescriptor( projectFile.toURI() );
    }

    // Code-facing properties

    @Override
    public void setProject( final Project project ) {
        assertNotExecuted();
        this.project = Lazy.from( project );
    }

    @Override
    public void setProjectDescriptor( final URI projectDescriptor ) {
        assertNotExecuted();
        this.project = new Lazy<Project>() {
            @Override
            public Project call() throws IOException, ParseException {
                return Project.parse( projectDescriptor, new WarnHandler( "project" ) );
            }
        };
    }

    protected Project project() throws BuildException {
        try {
            return this.project.get();
        } catch ( LazyInitializationException e ) {
            throw new BuildException( e.getCause() );
        }
    }

    protected ProjectResolver projectResolver() {
        try {
            return this.projectResolver.get();
        } catch ( LazyInitializationException e ) {
            throw new BuildException( e.getCause() );
        }
    }

    class WarnHandler implements IntelliJParserBase.Handler {
        private final String contextName;

        public WarnHandler( final String contextName ) {
            if ( contextName == null )
                throw new IllegalArgumentException( "The context name cannot be null." );
            this.contextName = contextName;
        }

        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
            if ( componentName == null )
                throw new IllegalArgumentException( "Component name cannot be null." );
            if ( componentNode == null )
                throw new IllegalArgumentException( "Component node cannot be null." );

            ProjectTaskBase.this.log( "Unrecognized " + this.contextName + " component \"" + componentName + "\"",
                    org.apache.tools.ant.Project.MSG_WARN );
        }
    }

    public final void assertProjectSpecified() throws BuildException {
        if ( project() == null )
            throw new BuildException( "Project descriptor or file ('projectdescriptor' and 'projectfile' " +
                    "attributes respectively) not specified." );
    }
}
