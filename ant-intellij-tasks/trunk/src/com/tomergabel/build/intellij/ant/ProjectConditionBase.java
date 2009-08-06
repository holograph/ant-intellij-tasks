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

public abstract class ProjectConditionBase extends ConditionBase implements ProjectReceiver {
    private Lazy<Project> project = Lazy.from( null );

    private final Lazy<ProjectResolver> projectResolver = new Lazy<ProjectResolver>() {
        @Override
        public ProjectResolver call() throws Exception {
            final Project project = ProjectConditionBase.this.project.get();
            return project != null ? new ProjectResolver( project ) : null;
        }
    };

    // Ant-facing properties

    @Override
    public void setProjectFile( final File projectFile ) {
        assertNotEvaluated();
        setProjectDescriptor( projectFile.toURI() );
    }

    // Code-facing properties

    @Override
    public void setProject( final Project project ) {
        assertNotEvaluated();
        this.project = Lazy.from( project );
    }

    @Override
    public void setProjectDescriptor( final URI projectDescriptor ) {
        assertNotEvaluated();
        this.project = new Lazy<Project>() {
            @Override
            public Project call() throws IOException, ParseException {
                return Project.parse( projectDescriptor, new IgnoreHandler() );
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

    public final void assertProjectSpecified() throws BuildException {
        if ( project() == null )
            throw new BuildException( "Project descriptor or file ('projectdescriptor' and 'projectfile' " +
                    "attributes respectively) not specified." );
    }

    static class IgnoreHandler implements IntelliJParserBase.Handler {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
        }
    }
}
