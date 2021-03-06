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

package com.tomergabel.build.intellij.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class ModuleDependency implements Dependency {
    public final String name;

    public ModuleDependency( final String name ) throws IllegalArgumentException {
        super();
        if ( name == null )
            throw new IllegalArgumentException( "Module name cannot be null." );
        this.name = name;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final ModuleDependency that = (ModuleDependency) o;

        return !( this.name != null ? !name.equals( that.name ) : that.name != null );

    }

    @Override
    public int hashCode() {
        return this.name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "module:" + this.name;
    }



    @Override
    public Collection<String> resolveClasspath( final ModuleResolver resolver, final boolean includeSources,
                                                final boolean includeTests )
            throws IllegalArgumentException, ResolutionException {
        if ( resolver == null )
            throw new IllegalArgumentException( "The module resolver cannot be null." );

        // Resolve the depenendee against the project
        final ProjectResolver project = resolver.getProjectResolver();
        if ( project == null )
            throw new ResolutionException(
                    "Cannot resolve module \"" + this.name + "\" against module \"" + resolver.getModule().getName() +
                            "\" because a project was not specified." );
        final ModuleResolver dependee = project.getModuleResolver( this.name );

        // A module dependency's classpath contribution comprises its own classpath, plus its compile output
        final Collection<String> classpath = new HashSet<String>();
        classpath.addAll( dependee.resolveModuleClasspath( includeSources, includeTests ) );
        if ( includeSources )
            classpath.add( dependee.resolveModuleOutputPath( false ) );
        if ( includeTests )
            classpath.add( dependee.resolveModuleOutputPath( true ) );
        return Collections.unmodifiableCollection( classpath );
    }
}
