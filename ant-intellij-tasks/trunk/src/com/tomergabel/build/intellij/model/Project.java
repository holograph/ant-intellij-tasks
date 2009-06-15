package com.tomergabel.build.intellij.model;

import com.tomergabel.util.UriUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public final class Project extends IntelliJParserBase {
    private final URI projectRoot;
    private boolean relativePaths;
    private String outputUrl;
    private String name;
    private Collection<String> modules = new HashSet<String>();
    private Map<String, Library> libraries = new HashMap<String, Library>();

    public static class Library {
        private final String name;
        private final Collection<String> classes;
        private final Collection<String> javadoc;
        private final Collection<String> sources;

        public Library( final String name ) {
            this.name = name;
            this.classes = new HashSet<String>();
            this.javadoc = new HashSet<String>();
            this.sources = new HashSet<String>();
        }

        public Collection<String> getClasses() {
            return Collections.unmodifiableCollection( this.classes );
        }

        public Collection<String> getJavadoc() {
            return Collections.unmodifiableCollection( this.javadoc );
        }

        public Collection<String> getSources() {
            return Collections.unmodifiableCollection( this.sources );
        }
    }

    private class ProjectRootManagerHandler implements Handler {
        @Override
        public void parse( String componentName, Node componentNode ) throws IllegalArgumentException, ParseException {
            // TODO language level
            // TODO assert keywords
            Project.this.outputUrl = extract( componentNode, "output/@url",
                    "Cannot extract project compiler output URL" );
        }
    }

    private class ProjectDetailsHandler implements Handler {
        @Override
        public void parse( String componentName, Node componentNode ) throws IllegalArgumentException, ParseException {
            Project.this.name = extract( componentNode, "option[@name=\"projectName\"]/@value",
                    "Cannot extract project name" );
        }
    }

    private class ProjectModuleManagerHandler implements Handler {
        @Override
        public void parse( String componentName, Node componentNode ) throws IllegalArgumentException, ParseException {
            for ( Node module : extractAll( componentNode, "modules/module", "Cannot extract module list" ) )
                Project.this.modules.add( extract( module, "@fileurl", "Cannot extract module file path" ) );
        }
    }

    private class LibraryTableHandler implements Handler {
        @Override
        public void parse( String componentName, Node componentNode ) throws IllegalArgumentException, ParseException {
            for ( Node libraryNode : extractAll( componentNode, "library", "Cannot extract libraries" ) ) {
                // Parse library data
                final Library library = new Library( extract( libraryNode, "@name", "Cannot extract library name" ) );
                iterateRoots( libraryNode, library.name, "CLASSES", library.classes );
                iterateRoots( libraryNode, library.name, "JAVADOC", library.javadoc );
                iterateRoots( libraryNode, library.name, "SOURCES", library.sources );
                Project.this.libraries.put( library.name, library );
            }
        }

        private void iterateRoots( final Node libraryNode, final String libraryName, final String node,
                                   final Collection<String> target )
                throws ParseException {
            for ( Node root : extractAll( libraryNode, node + "/root",
                    "Cannot extract " + node.toLowerCase() + " root paths for library \"" + libraryName + "\"" ) )
                target.add( extract( root, "@url",
                        "Cannot extract " + node.toLowerCase() + " root path URL for library \"" + libraryName +
                                "\"" ) );
        }
    }


    private final URI projectDescriptor;

    private Project( URI projectDescriptor, Handler defaultHandler ) throws IllegalArgumentException {
        super( defaultHandler );

        if ( projectDescriptor == null )
            throw new IllegalArgumentException( "Project descriptor cannot be null." );

        // Extract project root
        this.projectDescriptor = projectDescriptor;
        this.projectRoot = UriUtils.getParent( projectDescriptor );

        // Register handlers

        // Register ignored components
        registerComponentHandler( "AntConfiguration", ignoreHandler );
        registerComponentHandler( "BuildJarProjectSettings", ignoreHandler );               // TODO
        registerComponentHandler( "CodeStyleSettingsManager", ignoreHandler );
        registerComponentHandler( "CompilerConfiguration", ignoreHandler );                 // TODO
        registerComponentHandler( "CopyrightManager", ignoreHandler );
        registerComponentHandler( "DependencyValidationManager", ignoreHandler );
        registerComponentHandler( "EclipseCompilerSettings", ignoreHandler );
        registerComponentHandler( "Encoding", ignoreHandler );                              // TODO
        registerComponentHandler( "InspectionProjectProfileManager", ignoreHandler );
        registerComponentHandler( "JavacSettings", ignoreHandler );                         // TODO
        registerComponentHandler( "JavadocGenerationManager", ignoreHandler );              // TODO
        registerComponentHandler( "JikesSettings", ignoreHandler );
        registerComponentHandler( "ProjectFileVersion", ignoreHandler );
        registerComponentHandler( "ProjectKey", ignoreHandler );
        registerComponentHandler( "RmicSettings", ignoreHandler );
        registerComponentHandler( "SvnBranchConfigurationManager", ignoreHandler );
        registerComponentHandler( "VcsDirectoryMappings", ignoreHandler );                  // TODO
        registerComponentHandler( "IdProvider", ignoreHandler );
        registerComponentHandler( "Palette2", ignoreHandler );
        registerComponentHandler( "ResourceManagerContainer", ignoreHandler );              // TODO
        registerComponentHandler( "WebServicesPlugin", ignoreHandler );                     // TODO for package

        // Register handlers
        registerComponentHandler( "ProjectDetails", new ProjectDetailsHandler() );
        registerComponentHandler( "ProjectModuleManager", new ProjectModuleManagerHandler() );
        registerComponentHandler( "ProjectRootManager", new ProjectRootManagerHandler() );
        registerComponentHandler( "libraryTable", new LibraryTableHandler() );
    }

    public static Project parse( URI descriptor ) throws IllegalArgumentException, IOException, ParseException {
        return parse( descriptor, throwHandler );
    }

    public static Project parse( URI descriptor, Handler defaultHandler ) throws IllegalArgumentException, IOException, ParseException {
        if ( descriptor == null )
            throw new IllegalArgumentException( "The project descriptor URI cannot be null." );

        // Load document
        final Document document;
        try {
            document = builderFactory.newDocumentBuilder().parse( new File( descriptor ) );
        } catch ( SAXException e ) {
            throw new ParseException( "Cannot parse XML document, see inner exception for details.", e );
        } catch ( ParserConfigurationException e ) {
            throw new ParseException( "XML parser configuration invalid, see inner exception for details.", e );
        }

        // Instantiate project and parse relative paths flag
        final Project project = new Project( descriptor, defaultHandler );
        project.relativePaths = extract( document, "/project/@relativePaths", "Cannot extract relative paths flag" )
                .equals( "true" );

        // Parse all components
        project.processComponents( document );
        return project;
    }

    public boolean isRelativePaths() {
        return this.relativePaths;
    }

    public String getOutputUrl() {
        return this.outputUrl;
    }

    public String getName() {
        return this.name;
    }

    public Collection<String> getModules() {
        return Collections.unmodifiableCollection( this.modules );
    }

    public Map<String, Library> getLibraries() {
        return Collections.unmodifiableMap( this.libraries );
    }

    public URI getProjectRoot() {
        return projectRoot;
    }

    public URI getDescriptor() {
        return projectDescriptor;
    }

    @Override
    protected void generatePropertyMap( final Map<String, String> properties ) {
        properties.put( "PROJECT_DIR", this.projectRoot.getPath() );
    }

    @Override
    public String toString() {
        return "IntelliJ IDEA project \"" + this.name + "\"";
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final Project project = (Project) o;

        return relativePaths == project.relativePaths &&
                !( libraries != null ? !libraries.equals( project.libraries ) : project.libraries != null ) &&
                !( modules != null ? !modules.equals( project.modules ) : project.modules != null ) &&
                !( name != null ? !name.equals( project.name ) : project.name != null ) &&
                !( outputUrl != null ? !outputUrl.equals( project.outputUrl ) : project.outputUrl != null ) &&
                !( projectRoot != null ? projectRoot.compareTo( project.projectRoot ) != 0
                        : project.projectRoot != null );

    }

    @Override
    public int hashCode() {
        int result = projectRoot != null ? projectRoot.hashCode() : 0;
        result = 31 * result + ( relativePaths ? 1 : 0 );
        result = 31 * result + ( outputUrl != null ? outputUrl.hashCode() : 0 );
        result = 31 * result + ( name != null ? name.hashCode() : 0 );
        result = 31 * result + ( modules != null ? modules.hashCode() : 0 );
        result = 31 * result + ( libraries != null ? libraries.hashCode() : 0 );
        return result;
    }
}
