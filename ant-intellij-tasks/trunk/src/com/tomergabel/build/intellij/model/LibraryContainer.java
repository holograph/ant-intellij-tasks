package com.tomergabel.build.intellij.model;

import java.util.Map;

/**
 * As the name implies, a library container may contain {@link Library libraries} which are exposed via this interface.
 */
public interface LibraryContainer {
    /**
     * Returns all libraries in this container.
     * <p/>
     * The map returned by this method cannot be modified; any attempt to do so will result in an {@link
     * UnsupportedOperationException}.
     *
     * @return A read-only mapping of library names to their respective {@link Library library instances}.
     */
    Map<String, Library> getLibraries();
}
