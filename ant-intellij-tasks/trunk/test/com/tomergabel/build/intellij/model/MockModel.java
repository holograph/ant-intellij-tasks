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
        public static Lazy<Project> allModules = new LazyProjectLoader( "projects/all-modules.ipr" );
        public static Lazy<Project> buildOrderTest = new LazyProjectLoader( "projects/build-order-test.ipr" );
        public static Lazy<Project> outputSpecified = new LazyProjectLoader( "projects/output-specified.ipr" );
        public static Lazy<Project> outputUnspecified = new LazyProjectLoader( "projects/output-unspecified.ipr" );
        public static Lazy<Project> circularDependencyTest = new LazyProjectLoader( "projects/circular-dependency-test.ipr" );

        static class LazyProjectLoader extends LazyLoader<Project> {
            LazyProjectLoader( final String file ) {
                super( file );
            }

            @Override
            protected Project parse( final URI uri ) throws IOException, ParseException {
                return Project.parse( uri );
            }
        }
    }

    public static class Modules {
        public static Lazy<Module> selfContained = new LazyModuleLoader( "modules/self-contained.iml" );
        public static Lazy<Module> dependantModule = new LazyModuleLoader( "modules/dependant-module.iml" );
        public static Lazy<Module> dependantLibrary = new LazyModuleLoader( "modules/dependant-library.iml" );
        public static Lazy<Module> dependantBoth = new LazyModuleLoader( "modules/dependant-both.iml" );
        public static Lazy<Module> dependee = new LazyModuleLoader( "modules/dependee.iml" );
        public static Lazy<Module> outputModuleRelative = new LazyModuleLoader( "modules/output-module-relative.iml" );
        public static Lazy<Module> outputProjectRelative = new LazyModuleLoader( "modules/output-project-relative.iml" );
        public static Lazy<Module> outputUnspecified = new LazyModuleLoader( "modules/output-unspecified.iml" );
        public static Lazy<Module> buildOrderTestA = new LazyModuleLoader( "modules/build-order-test-a.iml" );
        public static Lazy<Module> buildOrderTestB = new LazyModuleLoader( "modules/build-order-test-b.iml" );
        public static Lazy<Module> buildOrderTestC = new LazyModuleLoader( "modules/build-order-test-c.iml" );
        public static Lazy<Module> buildOrderTestD = new LazyModuleLoader( "modules/build-order-test-d.iml" );
        public static Lazy<Module> circualrDependencyTestA = new LazyModuleLoader( "modules/circular-dependency-test-a.iml" );
        public static Lazy<Module> circualrDependencyTestB = new LazyModuleLoader( "modules/circular-dependency-test-b.iml" );

        static class LazyModuleLoader extends LazyLoader<Module> {
            LazyModuleLoader( final String file ) {
                super( file );
            }

            @Override
            protected Module parse( final URI uri ) throws IOException, ParseException {
                return Module.parse( uri );
            }
        }
    }

    public static Lazy<String> junitLibraryPath = new Lazy<String>() {
        @Override
        public String call() throws Exception {
            return UriUtils.getPath(
                    Projects.allModules.get().getProjectRoot().resolve( "libraries/junit/junit-4.6.jar" ) );
        }
    };
}
