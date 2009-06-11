package com.tomergabel.build.intellij;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Project extends IntelliJParserBase {
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

    private interface Handler {
        void parse( Node componentNode ) throws IllegalArgumentException, ParseException;
    }

    private class ProjectRootManagerHandler implements Handler {
        @Override
        public void parse( final Node componentNode ) throws IllegalArgumentException, ParseException {
            // TODO language level
            // TODO assert keywords
            Project.this.outputUrl = extract( componentNode, "output/@url",
                    "Cannot extract project compiler output URL" );
        }
    }

    private class ProjectDetailsHandler implements Handler {
        @Override
        public void parse( final Node componentNode ) throws IllegalArgumentException, ParseException {
            Project.this.name = extract( componentNode, "option[@name=\"projectName\"]/@value",
                    "Cannot extract project name" );
        }
    }

    private class ProjectModuleManagerHandler implements Handler {
        @Override
        public void parse( final Node componentNode ) throws IllegalArgumentException, ParseException {
            for ( Node module : extractAll( componentNode, "modules/module", "Cannot extract module list" ) )
                Project.this.modules.add( extract( module, "@fileurl", "Cannot extract module file path" ) );
        }
    }

    private class LibraryTableHandler implements Handler {
        @Override
        public void parse( final Node componentNode ) throws IllegalArgumentException, ParseException {
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


    private final HashMap<String, Handler> handlerMap;
    private final Handler defaultHandler;

    private Project() {
        // Build component handler map
        final Handler ignoreHandler = new Handler() {
            @Override
            public void parse( Node componentNode ) {
            }
        };

        this.handlerMap = new HashMap<String, Handler>();
        this.handlerMap.put( "AntConfiguration", ignoreHandler );
        this.handlerMap.put( "BuildJarProjectSettings", ignoreHandler );
        this.handlerMap.put( "CodeStyleSettingsManager", ignoreHandler );
        this.handlerMap.put( "CompilerConfiguration", ignoreHandler );
        this.handlerMap.put( "CopyrightManager", ignoreHandler );
        this.handlerMap.put( "DependencyValidationManager", ignoreHandler );
        this.handlerMap.put( "EclipseCompilerSettings", ignoreHandler );
        this.handlerMap.put( "Encoding", ignoreHandler );
        this.handlerMap.put( "InspectionProjectProfileManager", ignoreHandler );
        this.handlerMap.put( "JavacSettings", ignoreHandler );
        this.handlerMap.put( "JavadocGenerationManager", ignoreHandler );
        this.handlerMap.put( "JikesSettings", ignoreHandler );
        this.handlerMap.put( "ProjectDetails", new ProjectDetailsHandler() );
        this.handlerMap.put( "ProjectFileVersion", ignoreHandler );
        this.handlerMap.put( "ProjectKey", ignoreHandler );
        this.handlerMap.put( "ProjectModuleManager", new ProjectModuleManagerHandler() );
        this.handlerMap.put( "ProjectRootManager", new ProjectRootManagerHandler() );
        this.handlerMap.put( "RmicSettings", ignoreHandler );
        this.handlerMap.put( "SvnBranchConfigurationManager", ignoreHandler );
        this.handlerMap.put( "VcsDirectoryMappings", ignoreHandler );
        this.handlerMap.put( "libraryTable", new LibraryTableHandler() );

        // Set up default handler
        this.defaultHandler = new Handler() {
            @Override
            public void parse( Node componentNode ) throws ParseException {
                // TODO log warnings
                // TODO external configuration
                throw new ParseException(
                        "Unrecognized component " + componentNode.getAttributes().getNamedItem( "name" ) );
            }
        };

    }


    public static Project parse( File descriptor ) throws IllegalArgumentException, IOException, ParseException {
        // Load document
        final Document document;
        try {
            document = builderFactory.newDocumentBuilder().parse( descriptor );
        } catch ( SAXException e ) {
            throw new ParseException( "Cannot parse XML document, see inner exception for details.", e );
        } catch ( ParserConfigurationException e ) {
            throw new ParseException( "XML parser configuration invalid, see inner exception for details.", e );
        }

        // Instantiate project and parse relative paths flag
        final Project project = new Project();
        project.relativePaths = extract( document, "/project/@relativePaths", "Cannot extract relative paths flag" )
                .equals( "true" );

        // Parse all components
        for ( Node component : extractAll( document, "project/component", "Cannot extract project components" ) ) {
            final String componentName = extract( component, "@name", "Cannot extract component name" );
            final Handler handler =
                    project.handlerMap.containsKey( componentName ) ? project.handlerMap.get( componentName )
                            : project.defaultHandler;
            handler.parse( component );
        }

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
}
