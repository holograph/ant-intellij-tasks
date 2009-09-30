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

import com.tomergabel.util.UriUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

public class UriUtilsTests {
    @Test
    @Ignore( "Does not behave as intended, fix this!" )
    public void testGetParent_RootURL_ReturnsSame() throws URISyntaxException {
        final URI root = new URI( "file://c:/" );
        assertEquals( "Get parent on hierarchy root failed.", root, UriUtils.getParent( root ) );
    }

    @Test
    public void testGetParent_OneSegmentContainerURI_ReturnsRoot() throws URISyntaxException {
        final URI parent = new URI( "file://c:/" );
        final URI uri = new URI( "file://c:/temp/" );
        assertEquals( "Get parent on one-segment URI failed.", parent, UriUtils.getParent( uri ) );
    }
    
    @Test
    public void testGetParent_TwoSegmentContainerURI_ReturnsParent() throws URISyntaxException {
        final URI parent = new URI( "file://c:/temp/" );
        final URI uri = new URI( "file://c:/temp/test/" );
        assertEquals( "Get parent on two-segment URI failed.", parent, UriUtils.getParent( uri ) );
    }

    @Test
    public void testGetParent_TwoSegmentFileURI_ReturnsContainer() throws URISyntaxException {
        final URI parent = new URI( "file://c:/temp/test/" );
        final URI uri = new URI( "file://c:/temp/test/file.ext" );
        assertEquals( "Get parent on two-segment URI failed.", parent, UriUtils.getParent( uri ) );
    }

    @Test
    public void testGetFilename_WithTrailingSlash_ThrowsIllegalArgumentException()
            throws URISyntaxException, MalformedURLException {
        final URI uri = new URI( "file://c:/temp/test/" );
        try {
            UriUtils.getFilename( uri );
            fail( "Get filename failed on non-file." );
        } catch ( IllegalArgumentException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testGetFilename_ProperFileURI_FilenameReturned() throws URISyntaxException {
        final URI uri = new URI( "file://c:/temp/test/ham.and.eggs" );
        assertEquals( "Get filename failed on file.", "ham.and.eggs", UriUtils.getFilename( uri ) );
    }

    @Test
    public void testGetPath_AbsoluteFileURI_PathReturned() throws URISyntaxException, MalformedURLException {
        final URI uri = new URI( "file://c:/temp/test/ham.and.eggs" );
        assertEquals( "Get filename failed on file.", "c:\\temp\\test\\ham.and.eggs", UriUtils.getPath( uri ) );
    }
}
