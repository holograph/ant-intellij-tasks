package com.tomergabel.build.intellij.ant;

public class ModuleJarOutputSpecifiedCondition extends ModuleConditionBase {
    @Override
    protected boolean evaluate() {
        return module().isBuildJar();
    }
}
