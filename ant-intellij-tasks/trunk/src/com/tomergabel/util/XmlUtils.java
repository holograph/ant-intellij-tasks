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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class XmlUtils {
    private XmlUtils() {
    }

    public static Collection<Node> wrapNodeList( final NodeList nodeList ) throws IllegalArgumentException {
        if ( nodeList == null )
            throw new IllegalArgumentException( "Cannot wrap null NodeList" );

        return new Collection<Node>() {
            @Override
            public int size() {
                return nodeList.getLength();
            }

            @Override
            public boolean isEmpty() {
                return nodeList.getLength() > 0;
            }

            @Override
            public boolean contains( final Object o ) {
                if ( o == null || !( o instanceof Node ) )
                    return false;
                for ( int i = 0; i < nodeList.getLength(); ++i )
                    if ( o == nodeList.item( i ) )
                        return true;
                return false;
            }

            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return nodeList.getLength() > this.index;
                    }

                    @Override
                    public Node next() {
                        if ( this.index >= nodeList.getLength() )
                            throw new NoSuchElementException();
                        return nodeList.item( this.index++ );
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public Object[] toArray() {
                final Node[] array = new Node[nodeList.getLength()];
                for ( int i = 0; i < array.length; ++i )
                    array[ i ] = nodeList.item( i );
                return array;
            }

            @Override
            @SuppressWarnings( { "unchecked" } )
            public <T> T[] toArray( final T[] a ) throws ArrayStoreException {
                if ( !a.getClass().getComponentType().isAssignableFrom( Node.class ) )
                    throw new ArrayStoreException(
                            a.getClass().getComponentType().getName() + " is not the same or a supertype of Node" );

                if ( a.length >= nodeList.getLength() ) {
                    for ( int i = 0; i < nodeList.getLength(); ++i )
                        a[ i ] = (T) nodeList.item( i );
                    if ( a.length > nodeList.getLength() )
                        a[ nodeList.getLength() ] = null;
                    return a;
                }

                return (T[]) toArray();
            }

            @Override
            public boolean add( final Node node ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove( final Object o ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsAll( final Collection<?> c ) {
                for ( final Object o : c )
                    if ( !this.contains( o ) )
                        return false;
                return true;
            }

            @Override
            public boolean addAll( final Collection<? extends Node> c ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll( final Collection<?> c ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll( final Collection<?> c ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
