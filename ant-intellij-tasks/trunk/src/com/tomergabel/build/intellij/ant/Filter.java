package com.tomergabel.build.intellij.ant;

// See http://ant.apache.org/manual/develop.html#set-magic
// Ant supports enums as of 1.7.0, but requires case-sensitive matching.
// Until this is improved, modes are declared lower-cased. --TG
public enum Filter {
    source,
    test,
    both
}
