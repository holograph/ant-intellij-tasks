package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.util.Mapper;
import com.tomergabel.util.UriUtils;

// See http://ant.apache.org/manual/develop.html#set-magic
// Ant supports enums as of 1.7.0, but requires case-sensitive matching.
// Until this is improved, modes are declared lower-cased. --TG
public enum ResolutionModes {
    names( new Mapper<Module, Object>() {
        @Override
        public Object map( final Module source ) {
            return source.getName();
        }
    } ),

    descriptors( new Mapper<Module, Object>() {
        @Override
        public Object map( final Module source ) {
            return UriUtils.getPath( source.getModuleDescriptor() );
        }
    } );

    public final Mapper<Module, ?> mapper;

    private ResolutionModes( final Mapper<Module, ?> mapper ) {
        this.mapper = mapper;
    }
}
