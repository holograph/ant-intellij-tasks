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

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class AntTestBase {
    protected Project project;

    protected static final String DEFAULT_TASKS = "org/apache/tools/ant/taskdefs/defaults.properties";
    protected static final String DEFAULT_TYPES = "org/apache/tools/ant/types/defaults.properties";

    public AntTestBase() throws URISyntaxException, IOException {
        this.project = new Project();

        // Load type definitions
        final Properties typedefs = new Properties();
        typedefs.load( this.getClass().getClassLoader().getResourceAsStream( DEFAULT_TYPES ) );
        for ( final Object o : typedefs.keySet() )
            try {
                final Class clazz = Class.forName( (String) typedefs.get( o ) );
                this.project.addDataTypeDefinition( (String) o, clazz );
            } catch ( ClassNotFoundException ignored ) {
            }

        // Load task definitions
        final Properties taskdefs = new Properties();
        taskdefs.load( this.getClass().getClassLoader().getResourceAsStream( DEFAULT_TASKS ) );
        for ( final Object o : taskdefs.keySet() )
            try {
                final Class clazz = Class.forName( (String) taskdefs.get( o ) );
                this.project.addTaskDefinition( (String) o, clazz );
            } catch ( ClassNotFoundException ignored ) {
            }

        // Add build logger
        final DefaultLogger antLogger = new DefaultLogger();
        antLogger.setErrorPrintStream( System.err );
        antLogger.setOutputPrintStream( System.out );
        antLogger.setMessageOutputLevel( Project.MSG_VERBOSE );
        this.project.addBuildListener( antLogger );
    }
}
