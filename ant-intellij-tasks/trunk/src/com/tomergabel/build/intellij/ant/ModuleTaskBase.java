package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.*;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public abstract class ModuleTaskBase extends TaskBase {
    protected URI moduleDescriptor;
    protected URI projectDescriptor;
    protected Module module;
    protected Project project;

    // Ant-facing properties

    public void setSrcfile( final File srcfile ) {
        this.moduleDescriptor = srcfile.toURI();
    }

    public void setProjectFile( final File project ) {
        this.projectDescriptor = project.toURI();
    }

    // Code-facing properties

    public void setModule( final Module module ) {
        this.module = module;
    }

    public void setModuleDescriptor( final URI moduleDescriptor ) {
        this.moduleDescriptor = moduleDescriptor;
    }

    public void setProject( final Project project ) {
        this.project = project;
    }

    public void setProjectDescriptor( final URI projectDescriptor ) {
        this.projectDescriptor = projectDescriptor;
    }

    // Helper methods

    protected IntelliJParserBase.Handler warnHandler = new IntelliJParserBase.Handler() {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
            ModuleTaskBase.this.log( "Unrecognized component \"" + componentName + "\"",
                    org.apache.tools.ant.Project.MSG_WARN );
        }
    };

    protected Module module() throws BuildException {
        if ( this.module != null )
            return this.module;

        if ( this.moduleDescriptor != null )
            try {
                return Module.parse( this.moduleDescriptor, this.warnHandler );
            } catch ( IOException e ) {
                error( "Failed to read module file " + this.moduleDescriptor, e );
                return null;
            } catch ( ParseException e ) {
                error( "Error parsing module file " + this.moduleDescriptor, e );
                return null;
            }

        error( "Module file ('srcfile' attribute) not specified." );
        return null;
    }

    protected Project project() throws BuildException {
        if ( this.project != null )
            return this.project;

        if ( this.projectDescriptor != null )
            try {
                return Project.parse( this.projectDescriptor, this.warnHandler );
            } catch ( IOException e ) {
                error( "Failed to read project file " + this.projectDescriptor, e );
                return null;
            } catch ( ParseException e ) {
                error( "Error parsing project file " + this.projectDescriptor, e );
                return null;
            }

        return null;
    }

    protected Resolver resolver() throws BuildException {
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
