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

    // Ant-facing properties

    // See http://ant.apache.org/manual/develop.html#set-magic
    // Ant does not support URI properties, as least as far as the documentation is
    // concerned. Until this is improved, file it is. --TG

    public void setSrcfile( final File srcfile ) throws IllegalArgumentException {
        if ( srcfile == null )
            throw new IllegalArgumentException( "Null source file ('srcfile' attribute) cannot be specified." );

        setModuleDescriptor( srcfile.toURI() );
    }

    public void setModuleFile( final File moduleFile ) {
        if ( moduleFile == null )
            throw new IllegalArgumentException( "Null module file ('modulefile' attribute) cannot be specified." );

        setModuleDescriptor( moduleFile.toURI() );
    }

    public void setProjectFile( final File projectFile ) {
        setProjectDescriptor( projectFile.toURI() );
    }

    // Code-facing properties

    public void setModule( final Module module ) {
        this.module = Lazy.from( module );
    }

    public void setModuleDescriptor( final URI moduleDescriptor ) {
        this.module = new Lazy<Module>() {
            @Override
            public Module call() throws IOException, ParseException {
                return Module.parse( moduleDescriptor, ModuleTaskBase.this.warnHandler );
            }
        };
    }

    public void setProject( final Project project ) {
        this.project = Lazy.from( project );
    }

    public void setProjectDescriptor( final URI projectDescriptor ) {
        this.project = new Lazy<Project>() {
            @Override
            public Project call() throws IOException, ParseException {
                return Project.parse( projectDescriptor, ModuleTaskBase.this.warnHandler );
            }
        };
    }

    // Helper methods

    protected IntelliJParserBase.Handler warnHandler = new IntelliJParserBase.Handler() {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
            if ( componentName == null )
                throw new IllegalArgumentException( "Component name cannot be null." );
            if ( componentNode == null )
                throw new IllegalArgumentException( "Component node cannot be null." );

            ModuleTaskBase.this.log( "Unrecognized component \"" + componentName + "\"",
                    org.apache.tools.ant.Project.MSG_WARN );
        }
    };

    protected Module module() throws BuildException {
        try {
            return this.module.get();
        } catch ( LazyInitializationException e ) {
            final Throwable cause = e.getCause();   // Guaranteed not to be null
            if ( cause instanceof IOException )
                error( "Failed to load module file", cause );
            else if ( cause instanceof ParseException )
                error( "Error parsing module file", cause );
            else if ( cause instanceof BuildException )
                error( cause.getMessage() );
            else
                error( "Unexpected error occurred while parsing module file", cause );
            return null;
        }
    }

    protected Project project() throws BuildException {
        try {
            return this.project.get();
        } catch ( LazyInitializationException e ) {
            final Throwable cause = e.getCause();   // Guaranteed not to be null
            if ( cause instanceof IOException )
                error( "Failed to load project file", cause );
            else if ( cause instanceof ParseException )
                error( "Error parsing project file", cause );
            else if ( cause instanceof BuildException )
                error( cause.getMessage() );
            else
                error( "Unexpected error occurred while parsing project file", cause );
            return null;
        }
    }

    protected Resolver resolver() throws BuildException {
        // Resolve project and module.
        final Project project = project();
        final Module module = module();
        try {
            return new Resolver( project, module );
        } catch ( ResolutionException e ) {
            error( "Error parsing project file " + project.getDescriptor(), e );
            return null;
        }
    }
}
