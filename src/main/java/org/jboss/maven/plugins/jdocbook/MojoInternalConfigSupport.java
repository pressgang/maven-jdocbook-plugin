/*
 * jDocBook, processing of DocBook sources
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.jboss.maven.plugins.jdocbook;

import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

/**
 * Defines injection of config we will need.  Helps clean up the real code source.
 *
 * @author Steve Ebersole
 */
public abstract class MojoInternalConfigSupport extends AbstractMojo {
	/**
	 * INTERNAL : The project being built
	 *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

	/**
	 * INTERNAL : The artifacts associated with the dependencies defined as part
	 * of the project to which we are being attached.
	 *
	 * @parameter expression="${project.artifacts}"
     * @required
     * @readonly
	 */
	protected Set projectArtifacts;

	/**
	 * INTERNAL : The artifacts associated to the dependencies defined as part
	 * of our configuration within the project to which we are being attached.
	 *
	 * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
	 */
	protected List pluginArtifacts;

	/**
	 * INTERNAL : The representation of the maven execution.
	 *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
	protected MavenSession session;

	/**
	 * INTERNAL : The user settings (used to locate http proxy information).
	 *
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    protected Settings settings;

    /**
     * INTERNAL : The artifact repository to use.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * INTERNAL : The artifact metadata source to use.
     *
     * @component
     * @required
     * @readonly
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * INTERNAL : The artifact collector to use.
     *
     * @component
     * @required
     * @readonly
     */
    protected ArtifactCollector artifactCollector;

    /**
     * INTERNAL : used to get reference to environment Archiver/UnArchiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.manager.ArchiverManager}"
     * @required
     * @readonly
     */
    protected ArchiverManager archiverManager;

	/**
	 * INTERNAL : Artifact resolver, needed to download dependencies
	 *
	 * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
	 * @required
	 * @readonly
	 */
	protected ArtifactResolver artifactResolver;
}
