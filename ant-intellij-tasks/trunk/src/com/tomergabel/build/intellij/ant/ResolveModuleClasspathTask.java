package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;

public class ResolveModuleClasspathTask extends ModuleTaskBase {
    protected String pathId;
    protected SourceFilter filter = SourceFilter.source;

    public void setPathId( final String pathId ) {
        this.pathId = pathId;
    }

    public SourceFilter getFilter() {
        return this.filter;
    }

    public void setFilter( final SourceFilter filter ) {
        if ( filter == null )
            throw new IllegalArgumentException( "The source filter cannot be null." );
        this.filter = filter;
    }

    @Override
    public void executeTask() throws BuildException {
        if ( this.pathId == null )
            throw new BuildException( "Target path ID (attribute 'pathId') not specified." );

        logVerbose( "Resolving classpath for module '%s', filter=%s", module().getName(), this.filter );
        getProject().addReference( this.pathId, ant().buildClasspath( resolver(), this.filter ) );
    }
}
