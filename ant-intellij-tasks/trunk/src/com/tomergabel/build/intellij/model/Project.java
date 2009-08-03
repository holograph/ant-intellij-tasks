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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for IntelliJ IDEA project (.ipr) files.
 */
public final class Project extends IntelliJParserBase implements LibraryContainer {
    private final URI projectRoot;
    private boolean relativePaths;
    private String outputUrl;
    private String name;
    private final Collection<String> modules = new HashSet<String>();
    private final Map<String, Library> libraries = new HashMap<String, Library>();
    private final Collection<String> resourceExtensions = new HashSet<String>();
    private final Collection<String> resourceWildcardPatterns = new HashSet<String>();

    private class ProjectRootManagerHandler implements Handler {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
            // TODO language level
            // TODO assert keywords
            Project.this.outputUrl = extract( componentNode, "output/@url",
                    "Cannot extract project compiler output URL" );
        }
    }

    private class ProjectDetailsHandler implements Handler {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
            Project.this.name = extract( componentNode, "option[@name=\"projectName\"]/@value",
                    "Cannot extract project name" );
        }
    }

    private class ProjectModuleManagerHandler implements Handler {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
            for ( final Node module : extractAll( componentNode, "modules/module", "Cannot extract module list" ) )
                Project.this.modules.add( extract( module, "@fileurl", "Cannot extract module file path" ) );
        }
    }

    private class LibraryTableHandler implements Handler {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
            for ( final Node libraryNode : extractAll( componentNode, "library", "Cannot extract libraries" ) ) {
                final Library library = new Library( libraryNode );
                if ( library.getName() == null )
                    throw new ParseException( "Anonymous libraries cannot be specified on the project level." );
                Project.this.libraries.put( library.getName(), library );
            }
        }

    }

    static final Pattern extensionPattern = Pattern.compile( "\\.\\+\\\\\\.\\((.*)\\)" );

    private class CompilerConfigurationHandler implements Handler {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
            // TODO in the future: DEFAULT_COMPILER

            // Extract resource extensions
            for ( final Node extensionSet : extractAll( componentNode, "resourceExtensions/entry/@name",
                    "Cannot extract resource extensions." ) ) {
                // Try to match pattern
                final Matcher m = extensionPattern.matcher( extensionSet.getNodeValue() );
                if ( m.matches() )
                    Project.this.resourceExtensions
                            .addAll( Arrays.asList( m.group( 1 ).split( "\\|" ) ) );
                // TODO else emit parse warning (once warning infrastructure is in place)
            }

            // Extract wildcard resource patterns
            for ( final Node resourcePattern : extractAll( componentNode, "wildcardResourcePatterns/entry/@name",
                    "Cannot extract wilcard resource patterns." ) )
                Project.this.resourceWildcardPatterns.add( resourcePattern.getNodeValue() );
        }
    }


    private final URI projectDescriptor;

    private Project( final URI projectDescriptor, final Handler defaultHandler ) throws IllegalArgumentException {
        super( "project", defaultHandler );

        if ( projectDescriptor == null )
            throw new IllegalArgumentException( "Project descriptor cannot be null." );

        final String fileName = UriUtils.getFilename( projectDescriptor );
        if ( !fileName.toLowerCase().endsWith( ".ipr" ) )
            throw new IllegalArgumentException( "The specified project descriptor \"" + projectDescriptor +
                    "\" does not point to a valid IDEA project file (.ipr)" );

        // Extract project root
        this.projectDescriptor = projectDescriptor;
        this.projectRoot = UriUtils.getParent( projectDescriptor );

        // Register ignored components
        registerComponentHandler( "AntConfiguration", ignoreHandler );
        registerComponentHandler( "BuildJarProjectSettings", ignoreHandler );               // TODO
        registerComponentHandler( "CodeStyleSettingsManager", ignoreHandler );
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
        registerComponentHandler( "EntryPointsManager", ignoreHandler );
        registerComponentHandler( "UMLDiagramComponent", ignoreHandler );
        registerComponentHandler( "ResourceManagerContainer", ignoreHandler );              // TODO
        registerComponentHandler( "FacetAutodetectingManager", ignoreHandler );             // TODO
        registerComponentHandler( "WebServicesPlugin", ignoreHandler );                     // TODO for package

        // Register handlers
        registerComponentHandler( "ProjectDetails", new ProjectDetailsHandler() );
        registerComponentHandler( "ProjectModuleManager", new ProjectModuleManagerHandler() );
        registerComponentHandler( "ProjectRootManager", new ProjectRootManagerHandler() );
        registerComponentHandler( "libraryTable", new LibraryTableHandler() );
        registerComponentHandler( "CompilerConfiguration", new CompilerConfigurationHandler() );
    }

    public static Project parse( final URI descriptor ) throws IllegalArgumentException, IOException, ParseException {
        return parse( descriptor, throwHandler );
    }

    public static Project parse( final URI descriptor, final Handler defaultHandler )
            throws IllegalArgumentException, IOException, ParseException {
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
        project.relativePaths = "true"
                .equals( extract( document, "/project/@relativePaths", "Cannot extract relative paths flag" ) );

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

    @Override
    public Map<String, Library> getLibraries() {
        return Collections.unmodifiableMap( this.libraries );
    }

    public URI getProjectRoot() {
        return this.projectRoot;
    }

    public URI getDescriptor() {
        return this.projectDescriptor;
    }

    public Collection<String> getResourceExtensions() {
        return Collections.unmodifiableCollection( this.resourceExtensions );
    }

    public Collection<String> getResourceWildcardPatterns() {
        return Collections.unmodifiableCollection( this.resourceWildcardPatterns );
    }

    @Override
    protected void generatePropertyMap( final Map<String, String> properties ) {
        properties.put( "PROJECT_DIR", this.projectRoot.getPath() );
    }

    @Override
    public String toString() {
        return "IntelliJ IDEA project \"" + this.name + "\"";
    }

    @SuppressWarnings( { "RedundantIfStatement" } )
    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final Project project = (Project) o;

        if ( this.relativePaths != project.relativePaths ) return false;
        if ( this.libraries != null ? !libraries.equals( project.libraries ) : project.libraries != null ) return false;
        if ( this.modules != null ? !modules.equals( project.modules ) : project.modules != null ) return false;
        if ( this.name != null ? !name.equals( project.name ) : project.name != null ) return false;
        if ( this.outputUrl != null ? !outputUrl.equals( project.outputUrl ) : project.outputUrl != null ) return false;
        if ( this.projectRoot != null ? !projectRoot.equals( project.projectRoot ) : project.projectRoot != null )
            return false;
        if ( this.resourceExtensions != null ? !resourceExtensions.equals( project.resourceExtensions )
                : project.resourceExtensions != null ) return false;
        if ( this.resourceWildcardPatterns != null ? !resourceWildcardPatterns
                .equals( project.resourceWildcardPatterns )
                : project.resourceWildcardPatterns != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.projectRoot != null ? projectRoot.hashCode() : 0;
        result = 31 * result + ( this.relativePaths ? 1 : 0 );
        result = 31 * result + ( this.outputUrl != null ? outputUrl.hashCode() : 0 );
        result = 31 * result + ( this.name != null ? name.hashCode() : 0 );
        result = 31 * result + ( this.modules != null ? modules.hashCode() : 0 );
        result = 31 * result + ( this.libraries != null ? libraries.hashCode() : 0 );
        result = 31 * result + ( this.resourceExtensions != null ? resourceExtensions.hashCode() : 0 );
        result = 31 * result + ( this.resourceWildcardPatterns != null ? resourceWildcardPatterns.hashCode() : 0 );
        result = 31 * result + ( this.projectDescriptor != null ? projectDescriptor.hashCode() : 0 );
        return result;
    }
}
