package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.MockModel;
import com.tomergabel.util.LazyInitializationException;
import org.apache.tools.ant.BuildException;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ProjectConditionBaseTests {
    static class TestableCondition extends ProjectConditionBase {
        @Override
        protected boolean evaluate() {
            return false;
        }
    }
    private ProjectConditionBase condition;

    @Before
    public void setup() {
        this.condition = new TestableCondition();
    }

    @Test
    public void assertProjectSpecified_ProjectNotSpecified_BuildExceptionIsThrown() {
        try {
            this.condition.assertProjectSpecified();
            fail( "Project not specified, BuildException expected." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void assertProjectSpecified_ProjectSpecified_NothingHappens() throws LazyInitializationException {
        this.condition.setProject( MockModel.Projects.allModules.get() );
        this.condition.assertProjectSpecified();
    }

    @Test
    public void assertProjectSpecified_ProjectFileSpecified_NothingHappens() throws LazyInitializationException {
        this.condition.setProjectFile( new File( MockModel.Projects.allModules.get().getDescriptor() ) );
        this.condition.assertProjectSpecified();
    }

    @Test
    public void assertProjectSpecified_ProjectDescriptorSpecified_NothingHappens() throws LazyInitializationException {
        this.condition.setProjectDescriptor( MockModel.Projects.allModules.get().getDescriptor() );
        this.condition.assertProjectSpecified();
    }

    @Test
    public void setProject_AfterEvaluation_IllegalStateExceptionIsThrown() throws LazyInitializationException {
        this.condition.eval();
        try {
            this.condition.setProject( MockModel.Projects.allModules.get() );
            fail( "Project specified after condition was evaluated, IllegalStateException expected" );
        } catch ( IllegalStateException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void setProjectFile_AfterEvaluation_IllegalStateExceptionIsThrown() throws LazyInitializationException {
        this.condition.eval();
        try {
            this.condition.setProjectFile( new File( MockModel.Projects.allModules.get().getDescriptor() ) );
            fail( "Project specified after condition was evaluated, IllegalStateException expected" );
        } catch ( IllegalStateException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void setProjectDescriptor_AfterEvaluation_IllegalStateExceptionIsThrown() throws LazyInitializationException {
        this.condition.eval();
        try {
            this.condition.setProjectDescriptor( MockModel.Projects.allModules.get().getDescriptor() );
            fail( "Project specified after condition was evaluated, IllegalStateException expected" );
        } catch ( IllegalStateException e ) {
            // Expected, all is well
        }
    }
}
