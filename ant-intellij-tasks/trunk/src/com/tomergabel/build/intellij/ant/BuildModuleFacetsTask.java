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

import com.tomergabel.build.intellij.model.EjbFacet;
import com.tomergabel.build.intellij.model.Facet;
import com.tomergabel.build.intellij.model.WebFacet;
import org.apache.tools.ant.BuildException;

public class BuildModuleFacetsTask extends ModuleTaskBase {
    @Override
    protected void executeTask() throws BuildException {
        for ( final Facet facet : module().getFacets() ) {
            if ( facet instanceof EjbFacet ) {
            } else if ( facet instanceof WebFacet ) {

            }
        }
    }
}
