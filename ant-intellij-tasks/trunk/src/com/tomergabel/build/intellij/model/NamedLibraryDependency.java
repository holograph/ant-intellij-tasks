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

public class NamedLibraryDependency extends LibraryDependency {
    private final LibraryDependency.Level level;
    private final String name;

    public NamedLibraryDependency( final String name, final LibraryDependency.Level level )
            throws IllegalArgumentException {
        super();

        if ( name == null )
            throw new IllegalArgumentException( "The library name cannot be null." );
        if ( level == null )
            throw new IllegalArgumentException( "The library level cannot be null." );

        this.name = name;
        this.level = level;
    }

    @Override
    public LibraryDependency.Level getLevel() {
        return this.level;
    }

    @Override
    public Library resolveLibrary( final ModuleResolver resolver ) throws ResolutionException {
        if ( resolver.getProjectResolver() == null )
            throw new ResolutionException(
                    "Cannot resolve library '" + this.name + "', project not defined." );
        final Library library = resolver.getProjectResolver().getProject().getLibraries().get( this.name );
        if ( library == null )
            throw new ResolutionException( "Library '" + this.name + "' not defined in project." );
        return library;
    }

    @SuppressWarnings( { "RedundantIfStatement" } )
    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final NamedLibraryDependency that = (NamedLibraryDependency) o;

        if ( this.level != that.level ) return false;
        if ( this.name != null ? !name.equals( that.name ) : that.name != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.level != null ? level.hashCode() : 0;
        result = 31 * result + ( this.name != null ? name.hashCode() : 0 );
        return result;
    }

    @Override
    public String toString() {
        return level.toString().toLowerCase() + "-level library:" + this.name;
    }
}
