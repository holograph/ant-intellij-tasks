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

package com.tomergabel.build.intellij.model;

import java.util.Map;
import java.util.HashMap;

public enum PackagingMethod {
    COPY,
    JAR,
    JAR_AND_LINK;

    private static final Map<String, PackagingMethod> stringMap;

    static {
        stringMap = new HashMap<String, PackagingMethod>();
        stringMap.put( "1", COPY );
//        stringMap.put( "2", JAR );
//        stringMap.put( "3", JAR_AND_LINK );
        stringMap.put( "6", JAR_AND_LINK );
    }

    public static PackagingMethod parse( final String methodString ) {
        return methodString != null ? stringMap.get( methodString ) : null;
    }
}
