/*
 * jDocBook, processing of DocBook sources as a Maven plugin
 *
 * Copyright (c) 2009, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
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

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.PublishingSource;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.xslt.XSLTException;

/**
 * This mojo's responsibility within the plugin/packaging is to bundle the
 * individual formats into deployable formats.  The desicion  Note that some formats (PDF, e.g.) are
 * already deployable.
 * <p/>
 * After bundling, each bundle is then attached to the project
 *
 * @goal bundle
 * @phase package
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
public class PackageMojo extends AbstractDocBookMojo {
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void process() throws RenderingException, XSLTException {
		File projectArtifactFile = new File( project.getBuild().getOutputDirectory(), project.getBuild().getFinalName() + ".war" );
		JarArchiver archiver = new JarArchiver();
		archiver.setDestFile( projectArtifactFile );

		try {
			for ( PublishingSource source : getPublishingSources( true ) ) {
				for ( FormatPlan formatPlan : getFormatPlans() ) {
					archiver.addDirectory(
							new File( source.resolvePublishingDirectory(), formatPlan.getName() ),
							formatPlan.getName() + "/"
					);
				}
			}
			archiver.createArchive();
		}
		catch ( IOException e ) {
			throw new RenderingException( "Unable to create archive [" + projectArtifactFile.getAbsolutePath() + "]", e );
		}
		catch ( ArchiverException e ) {
			throw new RenderingException( "Unable to populate archive [" + projectArtifactFile.getAbsolutePath() + "]", e );
		}

		project.getArtifact().setFile( projectArtifactFile );
	}
}
