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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.archiver.ArchiveFileFilter;
import org.codehaus.plexus.archiver.ArchiveFilterException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.FilterEnabled;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.xslt.XSLTException;
import org.jboss.maven.shared.resource.ResourceDelegate;

/**
 * This mojo's responsibility within the plugin/packaging is to process resources
 * defined by various inputs, moving them into a staging directory for use
 * during XSLT processing.  This is needed because the DocBook XSLT only allow
 * defining a single <tt>img.src.path</tt> value; FOP only allows a single
 * <tt>fontBaseDir</tt> value; etc.
 *
 * @goal resources
 * @phase process-resources
 * @requiresDependencyResolution compile
 *
 * @author Steve Ebersole
 */
@SuppressWarnings({ "UnusedDeclaration" })
public class ResourceMojo extends AbstractDocBookMojo {
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void process() throws RenderingException, XSLTException {
		stageStyleSupportArtifacts();
		stageProjectResources();
	}


	// style resources ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private final ArchiveFileFilter[] styleEntryFilters = new ArchiveFileFilter[] {
			new MetaInfExclusionFilter(),
	};

	private void stageStyleSupportArtifacts() {
		for ( Artifact artifact : collectArtifactsByType( "jdocbook-style", true ) ) {
			getLog().debug( "processing support artifact : " + artifact.getId() );
			unpackSupportArtifact( artifact.getFile(), directoryLayout.getStagingDirectory(), styleEntryFilters );
		}
	}

	protected void unpackSupportArtifact(File file, File target, ArchiveFileFilter[] entryFilters) throws RenderingException {
		getLog().debug( "unpacking support artifact [" + file.getAbsolutePath() + "] to directory [" + target.getAbsolutePath() + "]" );
		try {
            if ( ! target.exists() ) {
				boolean created = target.mkdirs();
				if ( ! created ) {
					getLog().warn( "File-system reported problem creating directory " + target.getAbsolutePath() );
				}
			}
			UnArchiver unArchiver = archiverManager.getUnArchiver( "jar" );
			if ( FilterEnabled.class.isInstance( unArchiver ) ) {
				// try to save some disk space...
				( ( FilterEnabled ) unArchiver ).setArchiveFilters( Arrays.asList( entryFilters ) );
			}
            unArchiver.setSourceFile( file );
            unArchiver.setDestDirectory( target );
			unArchiver.extract();
		}
        catch ( NoSuchArchiverException e ) {
            throw new RenderingException( "Unknown archiver type", e );
        }
        catch ( ArchiverException e ) {
            throw new RenderingException( "Error unpacking file [" + file + "] to [" + target + "]", e );
        }
    }


	// project local resources ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private void stageProjectResources() throws RenderingException {
		if ( imageResource != null ) {
			new ResourceDelegate( project, new File( stagingDirectory(), "images" ), getLog() ).process( imageResource );
		}
		if ( cssResource != null ) {
			new ResourceDelegate( project, new File( stagingDirectory(), "css" ), getLog() ).process( cssResource );
		}
	}

	private File stagingDirectory() {
		return directoryLayout.getStagingDirectory();
	}


	// ArchiveFileFilter impls ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private static class MetaInfExclusionFilter implements ArchiveFileFilter {
		public boolean include(InputStream dataStream, String entryName) throws ArchiveFilterException {
			// exclude all META-INF entries.
			return ! entryName.toUpperCase().startsWith( "META-INF/" );
		}
	}

}
