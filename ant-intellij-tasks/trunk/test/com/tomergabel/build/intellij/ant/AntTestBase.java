package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.Project;

public class AntTestBase {
    protected Project project;

    public AntTestBase() {
        project = new Project();
        
    }
}
