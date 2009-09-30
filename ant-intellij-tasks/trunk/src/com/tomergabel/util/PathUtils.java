/*
	Copyright 2009 Tomer Gabel <tomer@tomergabel.com>

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


	ant-intellij-tasks project (http://code.google.com/p/ant-intellij-tasks/)

	$Id$
*/

package com.tomergabel.util;

import java.io.File;
import java.util.Deque;
import java.util.ArrayDeque;

public class PathUtils {
    private PathUtils() {
    }

    /**
     * Calculates the relative path between a specified root directory and a target path.
     *
     * @param root   The absolute path of the root directory.
     * @param target The path to the target file or directory.
     * @return The relative path between the specified root directory and the target path.
     * @throws IllegalArgumentException <ul><li>The root file cannot be null.</li><li>The target cannot be
     *                                  null.</li><li>The root file must be a directory.</li><li>The root file must be
     *                                  absolute.</li></ul>
     */
    public static String relativize( final File root, final File target ) throws IllegalArgumentException {
        if ( root == null )
            throw new IllegalArgumentException( "The root file cannot be null." );
        if ( target == null )
            throw new IllegalArgumentException( "The target cannot be null." );
        if ( !root.isDirectory() )
            throw new IllegalArgumentException( "The root file must be a directory." );
        if ( !root.isAbsolute() )
            throw new IllegalArgumentException( "The root file must be absolute." );
        if ( !target.isAbsolute() )
            return target.toString();

        if ( root.equals( target ) )
                return ".";
        
        // Deconstruct hierarchies
        final Deque<File> rootHierarchy = new ArrayDeque<File>();
        for ( File f = root; f != null; f = f.getParentFile() )
            rootHierarchy.push( f );
        final Deque<File> targetHierarchy = new ArrayDeque<File>();
        for ( File f = target; f != null; f = f.getParentFile() )
            targetHierarchy.push( f );

        // Trace common root
        while ( rootHierarchy.size() > 0 && targetHierarchy.size() > 0 &&
                rootHierarchy.peek().equals( targetHierarchy.peek() ) ) {
            rootHierarchy.pop();
            targetHierarchy.pop();
        }
        // Create relative path
        final StringBuilder sb = new StringBuilder( rootHierarchy.size() * 3 + targetHierarchy.size() * 32 );
        while ( rootHierarchy.size() > 0 ) {
            sb.append( ".." );
            rootHierarchy.pop();
            if ( rootHierarchy.size() > 0 || targetHierarchy.size() > 0 )
                sb.append( File.separator );
        }
        while ( targetHierarchy.size() > 0 ) {
            sb.append( targetHierarchy.pop().getName() );
            if ( targetHierarchy.size() > 0 )
                sb.append( File.separator );
        }
        return sb.toString();
    }
}
