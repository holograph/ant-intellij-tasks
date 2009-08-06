package com.tomergabel.build.intellij.ant.prototype;

import com.tomergabel.build.intellij.model.Module;

import java.io.File;
import java.net.URI;

public interface ModuleReceiver extends ProjectReceiver {
    void setModule( final Module module );
    void setModuleFile( final File moduleFile );
    void setModuleDescriptor( final URI moduleDescriptor );
}
