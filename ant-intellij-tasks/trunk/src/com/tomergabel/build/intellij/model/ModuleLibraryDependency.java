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

public class ModuleLibraryDependency extends LibraryDependency {
    private final Library library;

    public ModuleLibraryDependency( final Library library ) {
        super();
        this.library = library;
    }

    @Override
    public Level getLevel() {
        return Level.MODULE;
    }

    @Override
    public Library resolveLibrary( final ModuleResolver resolver ) throws ResolutionException {
        return this.library;
    }

    @SuppressWarnings( { "RedundantIfStatement" } )
    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final ModuleLibraryDependency that = (ModuleLibraryDependency) o;

        if ( this.library != null ? !library.equals( that.library ) : that.library != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return this.library != null ? library.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "module library" +
                ( this.library.getName() != null ? ( " \"" + this.library.getName() + "\"" ) : "" );
    }
}
