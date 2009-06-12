package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class BuildModuleTask extends Task {
    @Override
    public void execute() throws IllegalStateException, BuildException {
//        // Resolve output directory
//        final URI output;
//        try {
//            output = resolver.resolveUri( module.getOutputUrl() );
//        } catch ( ResolutionException e ) {
//            error( "Failed to resolve output directory", e );
//            return;
//        }

        // Resolve module dependencies
        // resolve classpath
        // build module sources
        // copy module resources                   
    }
 }
