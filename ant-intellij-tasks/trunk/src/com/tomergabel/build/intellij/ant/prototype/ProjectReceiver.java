package com.tomergabel.build.intellij.ant.prototype;

import com.tomergabel.build.intellij.model.Project;

import java.io.File;
import java.net.URI;

public interface ProjectReceiver {
    void setProject( final Project project );
    void setProjectFile( final File projectFile );
    void setProjectDescriptor( final URI projectDescriptor );
}
