package com.tomergabel.build.intellij.ant;

import static junit.framework.Assert.fail;
import org.junit.Test;

public class ConditionBaseTests {
    static class TestableCondition extends ConditionBase {
        @Override
        protected boolean evaluate() {
            return false;
        }
    }

    @Test
    public void assertNotEvaluated_BeforeEvaluation_NothingHappens() {
        final TestableCondition condition = new TestableCondition();
        condition.assertNotEvaluated();
    }

    @Test
    public void assertNotEvaluated_AfterEvaluation_IllegalStateExceptionIsThrown() {
        final TestableCondition condition = new TestableCondition();
        condition.assertNotEvaluated();         // sanity
        condition.eval();
        try {
            condition.assertNotEvaluated();
            fail( "Assertion succeeded, IllegalStateException expected." );
        } catch ( IllegalStateException e ) {
            // Expected, nothing to do here...
        }
    }
}
