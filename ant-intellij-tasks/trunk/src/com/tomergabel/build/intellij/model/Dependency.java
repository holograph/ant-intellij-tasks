package com.tomergabel.build.intellij.model;

import java.util.Collection;

/**
 * An abstract class providing a common base object for dependencies within an IntelliJ IDEA project.
 */
public interface Dependency {
    /**
     * Resolves the entries this dependency contributes to a dependent module's classpath.
     *
     * @param resolver The {@link com.tomergabel.build.intellij.model.ModuleResolver module resolver} against which to
     *                 resolve the classpath entries.
     * @return The classpath entries resolved against the specified module.
     * @throws ResolutionException An error has occurred while resolving the classpath.
     */
    Collection<String> resolveClasspath( final ModuleResolver resolver ) throws ResolutionException;
}
