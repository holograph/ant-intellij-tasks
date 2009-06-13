package com.tomergabel.util;

public class Tuple<L, R> {
    public final L left;
    public final R right;

    public Tuple( final L left, final R right ) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final Tuple tuple = (Tuple) o;
        return this.left == tuple.left && this.right == tuple.right;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + ( right != null ? right.hashCode() : 0 );
        return result;
    }
}
