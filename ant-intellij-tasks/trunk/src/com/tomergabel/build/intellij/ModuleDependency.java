package com.tomergabel.build.intellij;

class ModuleDependency extends Dependency {
    public final String name;

    public ModuleDependency( String name ) {
        this.name = name;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        ModuleDependency that = (ModuleDependency) o;

        if ( name != null ? !name.equals( that.name ) : that.name != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
