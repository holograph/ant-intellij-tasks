package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.BuildException;

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

    protected abstract boolean evaluate();
}
