package de.draexlmaier.bpm.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.filter.ExcludeRegExpPaths;

public class WebArchiveBuilder
{

    private final WebArchive archive;

    public WebArchiveBuilder(final String archiveName)
    {
        this.archive = ShrinkWrap.create(WebArchive.class, archiveName);
    }

    /**
     * get the {@link WebArchive}
     * 
     * @return {@link WebArchive}
     */
    public WebArchive getWebArchive()
    {
        return this.archive;
    }

    /**
     * adds all packages to the {@link WebArchive}. The packages added recursive, that means all sub-packages are also added.
     * 
     * @param packages packages to add
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addPackages(final String... packages)
    {
        this.archive.addPackages(true, packages);
        return this;
    }

    /**
     * adds all packages to the {@link WebArchive}. The packages added recursive, that means all sub-packages are also added.
     * 
     * @param filter filter for excluding files from being added to the archive
     * @param packages packages to add
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addPackages(final ExlusionFilter filter, final String... packages)
    {
        this.archive.addPackages(true, getExclusionFilter(filter), packages);
        return this;
    }

    /**
     * add classes to the archive
     * 
     * @param classes classes to add
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addClasses(final String... classes)
    {
        for(final String clazz : classes)
        {
            this.archive.addClass(clazz);
        }

        return this;
    }

    /**
     * Add libraries to the archive. The libraries are stored in WEB-INF/lib.
     * 
     * @param libraries libraries to add
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addLibraries(final File[] libraries)
    {
        this.archive.addAsLibraries(libraries);
        return this;
    }

    /**
     * remove files from the archive.
     * 
     * @param filePath path in the archive where the file to remove is
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder removeFile(final String filePath)
    {
        this.archive.delete(filePath);
        return this;
    }

    /**
     * adds a resource-file or resource directory to the archive. The destination path is the same as the resource path.
     * 
     * @param resourcer path to the resource directory or file to add
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addResource(final String resource)
    {
        addResource(resource, resource);
        return this;
    }

    /**
     * adds a resource-file or resource directory to the archive.
     * 
     * @param resource resourcer path to the resource directory or file to add
     * @param destination The target path within the archive in which to add the resource
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addResource(final String resource, final String destination)
    {
        try
        {
            final URL url = this.getClass().getResource(resource);
            if(url != null)
            {
                final File toAdd = new File(url.toURI());
                this.archive.addAsResource(toAdd, destination);
            }
        }
        catch(final URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

        return this;
    }

    /**
     * Looks for the common configuration files and adds them to the {@link WebArchive} if found. <br>
     * This method try's to add: test-persistence.xml, beans.xml, processes.xml, dozer.xml, dbunit xml files, wsdl xml files, BPMN process files.
     * 
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addCommonConfigFilesPortlet()
    {
        addWebContentDirectory();
        addCommonFiles();

        return this;
    }

    public WebArchiveBuilder addCommonConfigFilesStandalone()
    {
        addCommonFiles();
        final File jboss = new File("WebContent/WEB-INF/jboss-deployment-structure.xml");
        this.archive.addAsWebInfResource(jboss, "jboss-deployment-structure.xml");
        return this;
    }

    private void addCommonFiles()
    {
        addPersistenceXml("META-INF/test-persistence.xml");
        addBeansXml("META-INF/beans.xml");
        addProcessesXml("META-INF/test-processes.xml");
        addDBUnitFiles();
        addServiceFiles();
        addDozerXml("META-INF/dozer.xml");
        addWsdlFiles();
        addBpmnProcessFiles();
        addInitialDataSetupScript();
    }

    /**
     * Adds a file as persistence.xml file to the {@link WebArchive}.
     * 
     * @param filePath path to the persistence.xml file
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addPersistenceXml(final String filePath)
    {
        if(this.getClass().getResource("/" + filePath) != null)
        {
            this.archive.addAsResource(filePath, "META-INF/persistence.xml");
        }
        return this;
    }

    /**
     * Adds a file as beans.xml file to the {@link WebArchive}.
     * 
     * @param filePath path to the beans.xml file
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addBeansXml(final String filePath)
    {
        final File file = new File("src/test/resources/" + filePath);
        if(file.exists())
        {
            this.archive.delete("WEB-INF/beans.xml");
            this.archive.addAsWebInfResource(file, "beans.xml");
        }
        return this;
    }

    /**
     * Adds a file as dozer.xml file to the {@link WebArchive}.
     * 
     * @param filePath path to the dozer.xml file
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addDozerXml(final String filePath)
    {
        if(this.getClass().getResource("/" + filePath) != null)
        {
            this.archive.addAsResource(filePath, "META-INF/dozer.xml");
        }
        return this;
    }

    /**
     * add all files from a directory as DBUnit datasets to the {@link WebArchive}.
     * 
     * @param parentDir the directory containing the DBUnit datasets (doesn't add files recursively)
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addDBUnitFiles(final String parentDir, final String target)
    {
        addResource(parentDir, target);
        return this;
    }

    /**
     * add all files from /datasets' as DBUnit datasets to the {@link WebArchive}.
     * 
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addDBUnitFiles()
    {
        addDBUnitFiles("/datasets", "/");
        return this;
    }

    public WebArchiveBuilder addMockJavaMailFiles()
    {
        return addPackages("org.jvnet.mock_javamail");
    }

    /**
     * Adds all files found under /wsdl to the archive.
     * 
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addWsdlFiles()
    {
        addWsdlFiles("wsdl");
        return this;
    }

    /**
     * Adds all files found under /wsdl to the archive.
     * 
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addWsdlFiles(final String parent)
    {
        final File main = new File("src/main/resources/" + parent);
        if(main.exists())
        {
            this.archive.addAsResource(main);
        }

        final File test = new File("src/test/resources/" + parent);
        if(test.exists())
        {
            this.archive.addAsResource(test);
        }

        return this;
    }

    /**
     * Adds all files found under /process to the archive.
     * 
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addBpmnProcessFiles(final String parent)
    {
        final File main = new File("src/main/resources/" + parent);
        if(main.exists())
        {
            this.archive.addAsResource(main);
        }

        final File test = new File("src/test/resources/" + parent);
        if(test.exists())
        {
            this.archive.addAsResource(test);
        }

        return this;
    }

    public WebArchiveBuilder addServiceFiles()
    {
        final File main = new File("src/main/resources/META-INF/services");
        if(main.exists())
        {
            this.archive.addAsResource(main, "META-INF/services");
        }

        final File test = new File("src/test/resources/META-INF/services");
        if(test.exists())
        {
            this.archive.addAsResource(test, "META-INF/services");
        }

        return this;
    }

    /**
     * Adds all files found under /process to the archive.
     * 
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addBpmnProcessFiles()
    {
        addBpmnProcessFiles("process");
        return this;
    }

    /**
     * Adds a file as processes.xml file to the {@link WebArchive}.
     * 
     * @param filePath path to the processes.xml file
     */
    public WebArchiveBuilder addProcessesXml(final String filePath)
    {
        if(this.getClass().getResource("/" + filePath) != null)
        {
            this.archive.addAsResource(filePath, "META-INF/processes.xml");
        }
        return this;
    }

    public WebArchiveBuilder addInitialDataSetupScript()
    {
        if(this.getClass().getResource("/import.sql") != null)
        {
            this.archive.addAsResource("import.sql");
        }
        return this;
    }

    /**
     * Adds a directory to an {@link Archive}
     * 
     * @param directoryPath path to the directory that should be added
     * @param archivePath location where to add the directory in the {@link Archive}
     */
    public WebArchiveBuilder addDirectoryToArchive(final String directoryPath, final String archivePath)
    {
        this.archive.merge(
                ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class).importDirectory(directoryPath)
                        .as(GenericArchive.class), archivePath, Filters.includeAll());
        return this;
    }

    /**
     * add the complete WebContent directory to the war archive.
     * 
     * @return {@link WebArchiveBuilder}
     */
    public WebArchiveBuilder addWebContentDirectory()
    {
        addDirectoryToArchive("WebContent", "/");
        return this;
    }

    private ExcludeRegExpPaths getExclusionFilter(final ExlusionFilter filter)
    {
        return new ExcludeRegExpPaths(filter.getRegex());
    }

    public static class ExlusionFilter
    {
        private final StringBuilder regex = new StringBuilder();

        public ExlusionFilter exclude(final String expression)
        {
            this.regex.append(expression);
            this.regex.append("|");
            return this;
        }

        public static ExlusionFilter defaultExclusionFilter()
        {
            return new ExlusionFilter().exclude(".*/Test.*").exclude(".*/.*Test.class")
                    .exclude(".*de.draexlmaier.bpm.common.*");
        }

        public static ExlusionFilter withMocksExclusionFilter()
        {
            return defaultExclusionFilter().exclude(".*/Mock.*");
        }

        public static ExlusionFilter uiExclusionFilter()
        {
            return defaultExclusionFilter().exclude(".*de.draexlmaier.bpm.ui.*");
        }

        public static ExlusionFilter newInstance()
        {
            return new ExlusionFilter();
        }

        public ExlusionFilter getFilter()
        {
            return this;
        }

        private String getRegex()
        {
            return this.regex.toString();
        }
    }
}
