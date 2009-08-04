package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.MockModel;
import com.tomergabel.util.LazyInitializationException;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class BuildJarsOnMakeSpecifiedConditionTests {
    @Test
    public void evaluate_ValidProject_CorrectValueReturned() throws LazyInitializationException {
        final BuildJarsOnMakeSpecifiedCondition condition = new BuildJarsOnMakeSpecifiedCondition();
        condition.setProject( MockModel.Projects.allModules.get() );
        assertTrue( "Project setting evaluated incorrectly, expected true.", condition.eval() );
    }
}
