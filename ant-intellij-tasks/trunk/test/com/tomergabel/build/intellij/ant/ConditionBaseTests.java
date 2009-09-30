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
