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

import java.util.*;

public class CharPrefixTree implements Collection<String> {
    private static class Node {
        public Map<Character, Node> children = new HashMap<Character, Node>();
    }

    Node root = null;

    @Override
    public Iterator<String> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray( final T[] a ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return this.root != null;
    }

    @Override
    public boolean add( final String s ) {
        Node node = this.root;
        if ( node == null )
            node = this.root = new Node();

        boolean isNew = false;
        for ( int i = 0; i < s.length(); ++i ) {
            final char c = s.charAt( i );
            if ( node.children.containsKey( c ) )
                node = node.children.get( c );
            else {
                isNew = true;
                node.children.put( c, node = new Node() );
            }
        }
        return isNew;
    }

    @Override
    public boolean remove( final Object o ) {
        if ( o instanceof String ) {
            final String s = (String) o;
            Node node = this.root;

            for ( int i = 0; node != null && i < s.length() - 1; ++i )
                node = node.children.get( s.charAt( i ) );

            return node != null;
        } else throw new ClassCastException();
    }

    @Override
    public boolean containsAll( final Collection<?> c ) {
        for ( final Object o : c )
            if ( !contains( o ) )
                return false;
        return true;
    }

    @Override
    public boolean addAll( final Collection<? extends String> c ) {
        boolean any = false;
        for ( final String s : c )
            any |= add( s );
        return any;
    }

    @Override
    public boolean removeAll( final Collection<?> c ) {
        boolean any = false;
        for ( final Object o : c )
            any |= remove( o );
        return any;
    }

    @Override
    public boolean retainAll( final Collection<?> c ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        this.root = null;
    }

    @Override
    public boolean contains( final Object o ) {
        if ( o instanceof String ) {
            final String s = (String) o;
            if ( s.length() == 0 )
                return !isEmpty();

            Node node = this.root;
            for ( int i = 0; node != null && i < s.length(); ++i )
                node = node.children.get( s.charAt( i ) );
            return node != null;
        } else throw new ClassCastException();
    }
}
