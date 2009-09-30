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

package com.tomergabel.build.util;

import static com.tomergabel.util.PathUtils.relativize;
import static junit.framework.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.File;

public class PathUtilsTests {
    private File root;

    @Before
    public void setup() {
        this.root = new File( getClass().getResource( "." ).getFile() ).getParentFile();
    }

    @Test
    public void relativize_NullRoot_ThrowsIllegalArgumentException() {
        try {
            relativize( null, new File( this.root, "ham" ) );
            fail( "Null root specified, IllegalArgumentException expected." );
        } catch ( IllegalArgumentException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void relativize_NullTarget_ThrowsIllegalArgumentException() {
        try {
            relativize( this.root, null );
            fail( "Null target specified, IllegalArgumentException expected." );
        } catch ( IllegalArgumentException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void relativize_RelativeRoot_ThrowsIllegalArgumentException() {
        try {
            final File file = new File( "ham" );
            relativize( file, file );
            fail( "Relative root specified, IllegalArgumentException expected." );
        } catch ( IllegalArgumentException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void relativize_FileRoot_ThrowsIllegalArgumentException() {
        try {
            final File file = new File( this.root, "ham" );
            relativize( file, file );
            fail( "File root specified, IllegalArgumentException expected." );
        } catch ( IllegalArgumentException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void relativize_AbsoluteRootRelativeTarget_RelativePathReturned() {
        assertEquals( "ham", relativize( this.root, new File( "ham" ) ) );
    }

    @Test
    public void relativize_AbsoluteRootAbsoluteTargetInSameDirectory_TargetFileNameReturned() {
        assertEquals( "ham", relativize( this.root, new File( this.root, "ham" ).getAbsoluteFile() ) );
    }

    @Test
    public void relativize_AbsoluteRootAbsoluteTargetInDirectParent_RelativePathReturned() {
        assertEquals( ".." + File.separator + "ham",
                relativize( this.root, new File( root.getParentFile(), "ham" ).getAbsoluteFile() ) );
    }

    @Test
    public void relativize_AbsoluteRootAbsoluteTargetInDifferentBranch_RelativePathReturned() {
        assertEquals( ".." + File.separator + "ham" + File.separator + "eggs",
                relativize( this.root, new File( root.getParentFile(), "ham/eggs" ).getAbsoluteFile() ) );
    }

    @Test
    public void relativize_AbsoluteRootAbsoluteTargetIsSameAsRoot_RelativePathReturned() {
        assertEquals( ".", relativize( this.root, this.root ) );
    }

    @Test
    public void relativize_AbsoluteRootAbsoluteTargetIsDirectParent_RelativePathReturned() {
        assertEquals( "..", relativize( this.root, root.getParentFile() ) );
    }
}
