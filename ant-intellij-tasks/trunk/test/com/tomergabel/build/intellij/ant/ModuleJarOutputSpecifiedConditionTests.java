package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.MockModel;
import com.tomergabel.util.LazyInitializationException;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModuleJarOutputSpecifiedConditionTests {
    @Test
    public void eval_ModelWithNoJarOutputSpecified_EvaluatesToFalse() throws LazyInitializationException {
        final ModuleJarOutputSpecifiedCondition condition = new ModuleJarOutputSpecifiedCondition();
        condition.setModule( MockModel.Modules.selfContained.get() );
        assertFalse( "Condition did not evaluate correctly.", condition.eval() );
    }

    @Test
    public void eval_ModelWithJarOutputSpecified_EvaluatesToTrue() throws LazyInitializationException {
        final ModuleJarOutputSpecifiedCondition condition = new ModuleJarOutputSpecifiedCondition();
        condition.setModule( MockModel.Modules.jarOutputSelfContained.get() );
        assertTrue( "Condition did not evaluate correctly.", condition.eval() );
    }
}
