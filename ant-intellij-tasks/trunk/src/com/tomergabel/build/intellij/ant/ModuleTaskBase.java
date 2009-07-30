package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.*;
import com.tomergabel.util.Lazy;
import com.tomergabel.util.LazyInitializationException;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public abstract class ModuleTaskBase extends TaskBase {
    private boolean executed = false;
    private Lazy<Module> module = new Lazy<Module>() {
        @Override
        public Module call() throws Exception {
            // I prefer the more accurate 'moduledescriptor' attribute, hence the error
            // message does not mention 'srcfile'. Ant conventions require that 'srcfile'
            // should also be supported, but it doesn't mean I have promote it :-)
            throw new BuildException( "Module descriptor or file ('moduledescriptor' and 'modulefile' " +
                    "attributes respectively) not specified." );
        }
    };

    private Lazy<Project> project = Lazy.from( null );

    private final Lazy<ProjectResolver> projectResolver = new Lazy<ProjectResolver>() {
        @Override
        public ProjectResolver call() throws Exception {
            final Project project = ModuleTaskBase.this.project.get();
            return project != null ? new ProjectResolver( project ) : null;
        }
    };

    private final Lazy<ModuleResolver> moduleResolver = new Lazy<ModuleResolver>() {
        @Override
        public ModuleResolver call() throws Exception {
            final ProjectResolver projectResolver = ModuleTaskBase.this.projectResolver.get();
            final Module module = ModuleTaskBase.this.module.get();
            return projectResolver != null ? projectResolver.getModuleResolver( module ) : new ModuleResolver( module );
        }
    };

    // Ant-facing properties

    // See http://ant.apache.org/manual/develop.html#set-magic
    // Ant does not support URI properties, as least as far as the documentation is
    // concerned. Until this is improved, file it is. --TG

    public void setSrcfile( final File srcfile ) throws IllegalArgumentException {
        if ( srcfile == null )
            throw new IllegalArgumentException( "Null source file ('srcfile' attribute) cannot be specified." );
        assertNotExecuted();
        setModuleDescriptor( srcfile.toURI() );
    }

    private void assertNotExecuted() throws IllegalStateException {
        if ( this.executed )
            throw new IllegalStateException( "Task has already been executed." );
    }

    public void setModuleFile( final File moduleFile ) {
        if ( moduleFile == null )
            throw new IllegalArgumentException( "Null module file ('modulefile' attribute) cannot be specified." );
        assertNotExecuted();
        setModuleDescriptor( moduleFile.toURI() );
    }

    public void setProjectFile( final File projectFile ) {
        assertNotExecuted();
        setProjectDescriptor( projectFile.toURI() );
    }

    // Code-facing properties

    public void setModule( final Module module ) {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );
        assertNotExecuted();
        this.module = Lazy.from( module );
    }

    public void setModuleDescriptor( final URI moduleDescriptor ) {
        if ( moduleDescriptor == null )
            throw new IllegalArgumentException( "The module descriptor URI cannot be null." );
        assertNotExecuted();
        this.module = new Lazy<Module>() {
            @Override
            public Module call() throws IOException, ParseException {
                return Module.parse( moduleDescriptor, new WarnHandler( "module" ) );
            }
        };
    }

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

    @Override
    public final void execute() throws BuildException {
        assertNotExecuted();
        this.executed = true;
        executeTask();
    }

    protected abstract void executeTask() throws BuildException;

    // Helper methods

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

            ModuleTaskBase.this.log( "Unrecognized " + this.contextName + " component \"" + componentName + "\"",
                    org.apache.tools.ant.Project.MSG_WARN );
        }
    }

    protected Module module() throws BuildException {
        try {
            return this.module.get();
        } catch ( LazyInitializationException e ) {
            error( e.getCause() );
            return null;
        }
    }

    protected Project project() throws BuildException {
        try {
            return this.project.get();
        } catch ( LazyInitializationException e ) {
            error( e.getCause() );
            return null;
        }
    }

    protected ModuleResolver resolver() throws BuildException {
        try {
            return this.moduleResolver.get();
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
}
