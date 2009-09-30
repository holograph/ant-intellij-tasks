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
import com.tomergabel.build.intellij.model.ModuleResolver;
import com.tomergabel.build.intellij.model.ProjectResolver;
import com.tomergabel.build.intellij.model.ParseException;
import com.tomergabel.build.intellij.ant.prototype.ModuleReceiver;
import com.tomergabel.util.Lazy;
import com.tomergabel.util.LazyInitializationException;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public abstract class ModuleConditionBase extends ProjectConditionBase implements ModuleReceiver {
    private Lazy<Module> module = new Lazy<Module>() {
        @Override
        public Module call() throws Exception {
            // I prefer the more accurate 'moduledescriptor' attribute, hence the error
            // message does not mention 'srcfile'. Ant conventions require that 'srcfile'
            // should also be supported, but it doesn't mean I have promote it :-)
            throw new BuildException( "Module descriptor or file ('moduleDescriptor' and 'moduleFile' " +
                    "attributes respectively) not specified." );
        }
    };

    private final Lazy<ModuleResolver> moduleResolver = new Lazy<ModuleResolver>() {
        @Override
        public ModuleResolver call() throws Exception {
            final ProjectResolver projectResolver = ModuleConditionBase.this.projectResolver();
            final Module module = ModuleConditionBase.this.module.get();
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
        assertNotEvaluated();
        setModuleDescriptor( srcfile.toURI() );
    }

    @Override
    public void setModuleFile( final File moduleFile ) {
        if ( moduleFile == null )
            throw new IllegalArgumentException( "Null module file ('modulefile' attribute) cannot be specified." );
        assertNotEvaluated();
        setModuleDescriptor( moduleFile.toURI() );
    }

    // Code-facing properties

    @Override
    public void setModule( final Module module ) {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );
        assertNotEvaluated();
        this.module = Lazy.from( module );
    }

    @Override
    public void setModuleDescriptor( final URI moduleDescriptor ) {
        if ( moduleDescriptor == null )
            throw new IllegalArgumentException( "The module descriptor URI cannot be null." );
        assertNotEvaluated();
        this.module = new Lazy<Module>() {
            @Override
            public Module call() throws IOException, ParseException {
                return Module.parse( moduleDescriptor, new IgnoreHandler() );
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

    protected ModuleResolver moduleResolver() throws BuildException {
        try {
            return this.moduleResolver.get();
        } catch ( LazyInitializationException e ) {
            throw new BuildException( e.getCause() );
        }
    }
}
