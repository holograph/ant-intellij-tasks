package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.ModuleResolver;
import com.tomergabel.build.intellij.model.ParseException;
import com.tomergabel.build.intellij.model.ProjectResolver;
import com.tomergabel.build.intellij.ant.prototype.ModuleReceiver;
import com.tomergabel.util.Lazy;
import com.tomergabel.util.LazyInitializationException;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public abstract class ModuleTaskBase extends ProjectTaskBase implements ModuleReceiver {
    private Lazy<Module> module = new Lazy<Module>() {
        @Override
        public Module call() throws Exception {
            throw new BuildException( "Module descriptor or file ('moduleDescriptor' and 'moduleFile' " +
                    "attributes respectively) not specified." );
        }
    };

    private final Lazy<ModuleResolver> moduleResolver = new Lazy<ModuleResolver>() {
        @Override
        public ModuleResolver call() throws Exception {
            final ProjectResolver projectResolver = ModuleTaskBase.this.projectResolver();
            final Module module = ModuleTaskBase.this.module.get();
            return projectResolver != null ? projectResolver.getModuleResolver( module ) : new ModuleResolver( module );
        }
    };

    // Ant-facing properties

    // See http://ant.apache.org/manual/develop.html#set-magic
    // Ant does not support URI properties, as least as far as the documentation is
    // concerned. Until this is improved, file it is. --TG

    @Override
    public void setModuleFile( final File moduleFile ) {
        if ( moduleFile == null )
            throw new IllegalArgumentException( "Null module file ('modulefile' attribute) cannot be specified." );
        assertNotExecuted();
        setModuleDescriptor( moduleFile.toURI() );
    }

    // Code-facing properties

    @Override
    public void setModule( final Module module ) {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );
        assertNotExecuted();
        this.module = Lazy.from( module );
    }

    @Override
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

    // Helper methods

    protected Module module() throws BuildException {
        try {
            return this.module.get();
        } catch ( LazyInitializationException e ) {
            throw new BuildException( e.getCause() );
        }
    }

    protected ModuleResolver resolver() throws BuildException {
        try {
            return this.moduleResolver.get();
        } catch ( LazyInitializationException e ) {
            throw new BuildException( e.getCause() );
        }
    }
}
