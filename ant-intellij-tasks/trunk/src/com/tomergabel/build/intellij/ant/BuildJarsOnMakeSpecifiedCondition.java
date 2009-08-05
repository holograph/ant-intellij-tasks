package com.tomergabel.build.intellij.ant;

public class BuildJarsOnMakeSpecifiedCondition extends ProjectConditionBase {
    @Override
    protected boolean evaluate() {
        assertProjectSpecified();
        return project().isBuildJarsOnMake();
    }
}
