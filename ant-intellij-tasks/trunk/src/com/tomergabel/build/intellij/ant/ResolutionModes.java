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
import com.tomergabel.util.Mapper;
import com.tomergabel.util.UriUtils;

// See http://ant.apache.org/manual/develop.html#set-magic
// Ant supports enums as of 1.7.0, but requires case-sensitive matching.
// Until this is improved, modes are declared lower-cased. --TG
public enum ResolutionModes {
    names( new Mapper<Module, Object>() {
        @Override
        public Object map( final Module source ) {
            return source.getName();
        }
    } ),

    descriptors( new Mapper<Module, Object>() {
        @Override
        public Object map( final Module source ) {
            return UriUtils.getPath( source.getModuleDescriptor() );
        }
    } );

    public final Mapper<Module, ?> mapper;

    private ResolutionModes( final Mapper<Module, ?> mapper ) {
        this.mapper = mapper;
    }
}
