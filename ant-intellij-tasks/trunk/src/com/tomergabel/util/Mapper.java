package com.tomergabel.util;

public interface Mapper<T, U> {
    U map( T source );
}
