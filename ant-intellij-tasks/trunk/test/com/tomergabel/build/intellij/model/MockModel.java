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

    abstract static class LazyLoader<T> extends Lazy<T> {
        String file;

        LazyLoader( final String file ) {
            super();
            this.file = file;
        }

        @Override
        public T call() throws Exception {
            final URI uri = MockModel.class.getResource( this.file ).toURI();
            return parse( uri );
        }

        protected abstract T parse( final URI uri ) throws Exception;
    }

    public static class Projects {
        public static Lazy<Project> allModules;
        public static Lazy<Project> outputSpecified;
        public static Lazy<Project> outputUnspecified;

        static class LazyProjectLoader extends LazyLoader<Project> {
            LazyProjectLoader( final String file ) {
                super( file );
            }

            @Override
            protected Project parse( final URI uri ) throws IOException, ParseException {
                return Project.parse( uri );
            }
        }

        static {
            allModules = new LazyProjectLoader( "projects/all-modules.ipr" );
            outputSpecified = new LazyProjectLoader( "projects/output-specified.ipr" );
            outputUnspecified = new LazyProjectLoader( "projects/output-unspecified.ipr" );
        }
    }

    public static class Modules {
        public static Lazy<Module> selfContained;
        public static Lazy<Module> dependantModule;
        public static Lazy<Module> dependantLibrary;
        public static Lazy<Module> dependantBoth;
        public static Lazy<Module> dependee;
        public static Lazy<Module> outputModuleRelative;
        public static Lazy<Module> outputProjectRelative;
        public static Lazy<Module> outputUnspecified;

        static class LazyModuleLoader extends LazyLoader<Module> {
            LazyModuleLoader( final String file ) {
                super( file );
            }

            @Override
            protected Module parse( final URI uri ) throws IOException, ParseException {
                return Module.parse( uri );
            }
        }

        static {
            selfContained = new LazyModuleLoader( "modules/self-contained.iml" );
            dependantModule = new LazyModuleLoader( "modules/dependant-module.iml" );
            dependantLibrary = new LazyModuleLoader( "modules/dependant-library.iml" );
            dependantBoth = new LazyModuleLoader( "modules/dependant-both.iml" );
            dependee = new LazyModuleLoader( "modules/dependee.iml" );
            outputModuleRelative = new LazyModuleLoader( "modules/output-module-relative.iml" );
            outputProjectRelative = new LazyModuleLoader( "modules/output-project-relative.iml" );
            outputUnspecified = new LazyModuleLoader( "modules/output-unspecified.iml" );
        }
    }

    public static Lazy<String> junitLibraryPath;

    static {
        junitLibraryPath = new Lazy<String>() {
            @Override
            public String call() throws Exception {
                return UriUtils.getPath(
                        Projects.allModules.get().getProjectRoot().resolve( "libraries/junit/junit-4.6.jar" ) );
            }
        };
    }
}
