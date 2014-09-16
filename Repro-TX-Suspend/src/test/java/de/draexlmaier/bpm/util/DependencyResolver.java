package de.draexlmaier.bpm.util;

import java.io.File;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;

public class DependencyResolver
{
    /**
     * Resolves dependencies from the pom.xml file if the project.
     * 
     * @param transitive if true transitive dependencies will also be resolved
     * @param includeTestDependencies if true dependencies with test scope will also be resolved.
     * @return the dependencies as array of {@link File} objects
     */
    public static File[] resolveMavenDependencies(final boolean transitive, final boolean includeTestDependencies)
    {
        return resolveMavenDependencies("pom.xml", transitive, includeTestDependencies);
    }

    /**
     * Resolves dependencies from a pom.xml file.
     * 
     * @param pomFilePath Path to the pom file
     * @param transitive if true transitive dependencies will also be resolved
     * @param includeTestDependencies if true dependencies with test scope will also be resolved.
     * @return the dependencies as array of {@link File} objects
     */
    public static File[] resolveMavenDependencies(final String pomFilePath, final boolean transitive,
            final boolean includeTestDependencies)
    {
        return resolveMavenDependencies(pomFilePath, transitive, includeTestDependencies, false);
    }

    /**
     * Resolves dependencies from a pom.xml file.
     * 
     * @param pomFilePath Path to the pom file
     * @param transitive if true transitive dependencies will also be resolved
     * @param includeTestDependencies if true dependencies with test scope will also be resolved.
     * @return the dependencies as array of {@link File} objects
     */
    public static File[] resolveMavenDependencies(final String pomFilePath, final boolean transitive,
            final boolean includeTestDependencies, final boolean offline)
    {
        final PomEquippedResolveStage oPom = getPomResolver(pomFilePath, offline);

        if(includeTestDependencies)
        {
            oPom.importDependencies(ScopeType.COMPILE, ScopeType.TEST);
        }
        else
        {
            oPom.importDependencies(ScopeType.COMPILE);
        }

        if(transitive)
        {
            return oPom.resolve().withTransitivity().asFile();
        }
        else
        {
            return oPom.resolve().withoutTransitivity().asFile();
        }
    }

    /**
     * Resolves a dependency from a pom.xml file.
     * 
     * @param pomFilePath pom.xml where the dependency is listed
     * @param transitive if true, also transitive dependencies will be added
     * @param offline if true only the local maven repository is used, else the remote repositories
     * @param dependency the dependency to add. Format: groupId:artifactId:version
     */
    public static File[] resolveMavenDependency(final String pomFilePath, final boolean transitive,
            final boolean offline, final String dependency)
    {
        final MavenStrategyStage resolver = getPomResolver(pomFilePath, offline).resolve(dependency);

        File[] files = null;
        if(transitive)
        {
            files = resolver.withTransitivity().asFile();
        }
        else
        {
            files = resolver.withoutTransitivity().asFile();
        }

        return files;
    }

    private static PomEquippedResolveStage getPomResolver(final String pomFilePath, final boolean offline)
    {
        if(offline)
        {
            return Maven.resolver().offline().loadPomFromFile(pomFilePath);
        }

        return Maven.resolver().loadPomFromFile(pomFilePath);
    }
}
