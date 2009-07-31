package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.IntelliJParserBase;
import com.tomergabel.build.intellij.model.ParseException;
import com.tomergabel.build.intellij.model.Project;
import com.tomergabel.build.intellij.model.ProjectResolver;
import com.tomergabel.util.Lazy;
import com.tomergabel.util.LazyInitializationException;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public abstract class ProjectTaskBase extends TaskBase {
    protected Lazy<Project> project = Lazy.from( null );

    protected final Lazy<ProjectResolver> projectResolver = new Lazy<ProjectResolver>() {
        @Override
        public ProjectResolver call() throws Exception {
            final Project project = ProjectTaskBase.this.project.get();
            return project != null ? new ProjectResolver( project ) : null;
        }
    };

    // Ant-facing properties

    public void setProjectFile( final File projectFile ) {
        assertNotExecuted();
        setProjectDescriptor( projectFile.toURI() );
    }

    // Code-facing properties
    
    public void setProject( final Project project ) {
        assertNotExecuted();
        this.project = Lazy.from( project );
    }

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
            error( e.getCause() );
            return null;
        }
    }

    protected ProjectResolver projectResolver() {
        try {
            return this.projectResolver.get();
        } catch ( LazyInitializationException e ) {
            error( e.getCause() );
            return null;
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

    public boolean assertProjectSpecified() {
        if ( project() == null ) {
            error( "Project descriptor or file ('projectdescriptor' and 'projectfile' " +
                "attributes respectively) not specified." );
            return false;
        }
        return true;
    }
}
