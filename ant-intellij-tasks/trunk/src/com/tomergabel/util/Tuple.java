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
        int result = this.left != null ? left.hashCode() : 0;
        result = 31 * result + ( this.right != null ? right.hashCode() : 0 );
        return result;
    }
}
