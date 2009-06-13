package com.tomergabel.build.intellij.model;

import com.tomergabel.util.Lazy;
import com.tomergabel.util.UriUtils;

import java.io.IOException;
import java.net.URI;

public final class MockModel {
    private MockModel() {
    }

    // ------------------------------------------------------
    // Support code
    // ------------------------------------------------------

    static abstract class LazyLoader<T> extends Lazy<T> {
        String file;

        LazyLoader( String file ) {
            this.file = file;
        }

        @Override
        public T call() throws Exception {
            final URI uri = MockModel.class.getResource( this.file ).toURI();
            return parse( uri );
        }

        protected abstract T parse( final URI uri ) throws Exception;
    }

    static class LazyModuleLoader extends LazyLoader<Module> {
        LazyModuleLoader( String file ) {
            super( file );
        }

        @Override
        protected Module parse( final URI uri ) throws IOException, ParseException {
            return Module.parse( uri );
        }
    }

    public static Lazy<Project> project;
    public static Lazy<Module> selfContained;
    public static Lazy<Module> dependantModule;
    public static Lazy<Module> dependantLibrary;
    public static Lazy<Module> dependantBoth;
    public static Lazy<Module> dependee;
    public static Lazy<String> junitLibraryPath;

    static {
        project = new LazyLoader<Project>( "project.ipr" ) {
            @Override
            protected Project parse( final URI uri ) throws Exception {
                return Project.parse( uri );
            }
        };
        selfContained = new LazyModuleLoader( "modules/self-contained.iml" );
        dependantModule = new LazyModuleLoader( "modules/dependant-module.iml" );
        dependantLibrary = new LazyModuleLoader( "modules/dependant-library.iml" );
        dependantBoth = new LazyModuleLoader( "modules/dependant-both.iml" );
        dependee = new LazyModuleLoader( "modules/dependee.iml" );
        junitLibraryPath = new Lazy<String>() {
            @Override
            public String call() throws Exception {
                return UriUtils
                        .getPath( project.get().getProjectRoot().resolve( "libraries/junit/junit-4.6.jar" ) );
            }
        };
    }
}
