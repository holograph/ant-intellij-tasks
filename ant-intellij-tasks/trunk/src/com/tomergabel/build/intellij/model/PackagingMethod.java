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
        stringMap.put( "2", JAR );
        stringMap.put( "3", JAR_AND_LINK );
        stringMap.put( "6", COPY );
    }

    public static PackagingMethod parse( final String methodString ) {
        return methodString != null ? stringMap.get( methodString ) : null;
    }
}
