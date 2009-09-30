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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;

public abstract class ConditionBase implements Condition {
    protected boolean evaluated = false;

    protected void assertNotEvaluated() {
        if ( this.evaluated )
            throw new IllegalStateException( "Task has already been executed." );
    }

    @Override
    public final boolean eval() throws BuildException {
        this.evaluated = true;
        return evaluate();
    }

    protected abstract boolean evaluate() throws BuildException;
}
