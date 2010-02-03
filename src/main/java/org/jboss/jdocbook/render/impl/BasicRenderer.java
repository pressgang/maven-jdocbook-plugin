/*
 * jDocBook, processing of DocBook sources as a Maven plugin
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
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
package org.jboss.jdocbook.render.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.jboss.jdocbook.Log;
import org.jboss.jdocbook.render.PublishingSource;
import org.jboss.jdocbook.render.Renderer;
import org.jboss.jdocbook.render.RenderingEnvironment;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.util.FileUtils;
import org.jboss.jdocbook.util.LocaleUtils;
import org.jboss.jdocbook.util.ConsoleRedirectionHandler;
import org.jboss.jdocbook.xslt.XSLTException;
import org.xml.sax.EntityResolver;

/**
 * The basic implementation of the {@link Renderer} contract.
 *
 * @author Steve Ebersole
 */
public class BasicRenderer implements Renderer {
	protected final RenderingEnvironment environment;
	protected final EntityResolver entityResolver;
	protected final FormatPlan formatPlan;


	/**
	 * Construct a renderer instance using the given <tt>options</tt>.
	 *
	 * @param environment execution environment
	 * @param entityResolver The entity resolver to use.
	 * @param formatPlan The formating plan/options
	 */
	public BasicRenderer(RenderingEnvironment environment, EntityResolver entityResolver, FormatPlan formatPlan) {
		this.environment = environment;
		this.entityResolver = entityResolver;
		this.formatPlan = formatPlan;
	}

	protected Log getLog() {
		return environment.log();
	}

	public void render() throws RenderingException, XSLTException {
		for ( PublishingSource source : environment.getPublishingSources( true ) ) {
			final String lang = stringify( source.getLocale() );
			getLog().trace( "Starting generation " + lang );

			// determine the source file from which to render
			File sourceFile = environment.getProfilingConfiguration().isEnabled()
					? source.resolveProfiledDocumentFile()
					: source.resolveDocumentFile();
			if ( !sourceFile.exists() ) {
				getLog().info( "Source document [" + sourceFile.getAbsolutePath() + "] did not exist; skipping" );
				continue;
			}

			final File publishingDirectory = source.resolvePublishingDirectory();
			if ( ! publishingDirectory.exists() ) {
				boolean created = publishingDirectory.mkdirs();
				if ( !created ) {
					getLog().info( "Unable to create publishing directory {}", publishingDirectory.getAbsolutePath() );
				}
			}

			getLog().info( "Processing " + lang + " -> " + formatPlan.getName() );
			render( sourceFile, formatPlan, publishingDirectory, environment.getStagingDirectory(), source );
		}
	}

	private String stringify(Locale locale) {
		return LocaleUtils.render( locale, environment.getOptions().getLocaleSeparator() );
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(File sourceFile, FormatPlan formatPlan, File renderingDirectory, File stagingDirectory, PublishingSource source) throws RenderingException, XSLTException {
		File targetDirectory = new File( renderingDirectory, formatPlan.getName() );
		if ( ! targetDirectory.exists() ) {
			FileUtils.mkdir( targetDirectory.getAbsolutePath() );
		}

		if ( formatPlan.isImageCopyingRequired() ) {
			if ( stagingDirectory.exists() ) {
				File imageBase = new File( stagingDirectory, "images" );
				if ( imageBase.exists() ) {
					try {
						FileUtils.copyDirectoryStructure( imageBase, targetDirectory );
					}
					catch ( IOException e ) {
						throw new RenderingException( "unable to copy images", e );
					}
				}
				File cssBase = new File( stagingDirectory, "css" );
				if ( cssBase.exists() ) {
					try {
						FileUtils.copyDirectoryStructure( cssBase, targetDirectory );
					}
					catch ( IOException e ) {
						throw new RenderingException( "unable to copy css", e );
					}
				}
			}
		}

		File targetFile = new File( targetDirectory, deduceTargetFileName( sourceFile, formatPlan ) );
		if ( targetFile.exists() ) {
			boolean deleted = targetFile.delete();
			if ( !deleted ) {
				getLog().info( "Unable to delete existing target file " + targetFile.getAbsolutePath() );
			}
		}
		if ( !targetFile.exists() ) {
			try {
				boolean created = targetFile.createNewFile();
				if ( !created ) {
					getLog().info( "Unable to create target file [{}]", targetFile.getAbsolutePath() );
				}
			}
			catch ( IOException e ) {
				throw new RenderingException( "unable to create output file [" + targetFile.getAbsolutePath() + "]", e );
			}
		}

		performRendering( sourceFile, formatPlan, stagingDirectory, targetFile , source );
	}

	private void performRendering(File sourceFile, FormatPlan formatPlan, File stagingDirectory, File targetFile, PublishingSource source) {
		Transformer transformer = buildTransformer( targetFile, formatPlan, stagingDirectory );
		String lang = LocaleUtils.render( source.getLocale(), environment.getOptions().getLocaleSeparator() );
		transformer.setParameter( "l10n.gentext.language", lang );

		ConsoleRedirectionHandler console = new ConsoleRedirectionHandler( determineConsoleRedirectFile( source, formatPlan ) );
		console.start();

		try {
			Source transformationSource = buildSource( sourceFile );
			Result transformationResult = buildResult( targetFile );

			try {
				transformer.transform( transformationSource, transformationResult );
			}
			catch ( TransformerException e ) {
				throw new XSLTException( "error rendering [" + e.getMessageAndLocation() + "] on " + sourceFile.getName(), e );
			}
			finally {
				releaseResult( transformationResult );
			}
		}
		finally {
			console.stop();
		}
	}

	private File determineConsoleRedirectFile(PublishingSource source, FormatPlan formatPlan) {
		String fileName = "console-"
				+ stringify( source.getLocale() ) + "-"
				+ formatPlan.getName()
				+ ".log";
		return new File( new File( environment.getWorkDirectory(), "log" ), fileName );
	}

	private String deduceTargetFileName(File source, FormatPlan formatPlan) {
		return formatPlan.getTargetNamingStrategy().determineTargetFileName( source );
	}

	protected Transformer buildTransformer(File targetFile, FormatPlan formatPlan, File stagingDirectory) throws RenderingException, XSLTException {
		final URL transformationStylesheet =  environment.getResourceHelper().requireResource( formatPlan.getStylesheetResource() );
		Transformer transformer = environment.getTransformerBuilder().buildTransformer( formatPlan, transformationStylesheet );
		if ( formatPlan.isImagePathSettingRequired() ) {
			try {
				String imgSrcPath = new File( stagingDirectory, "images" ).toURI().toURL().toString();
				if ( !imgSrcPath.endsWith( "/" ) ) {
					imgSrcPath += '/';
				}
				getLog().trace( "setting 'img.src.path' xslt parameter [" + imgSrcPath + "]" );
				transformer.setParameter( "img.src.path", imgSrcPath );
			}
			catch ( MalformedURLException e ) {
				throw new XSLTException( "unable to prepare 'img.src.path' xslt parameter", e );
			}
		}

		transformer.setParameter( "keep.relative.image.uris", environment.getOptions().isUseRelativeImageUris() ? "1" : "0" );
		transformer.setParameter( "base.dir", targetFile.getParent() + File.separator );
		transformer.setParameter( "manifest.in.base.dir", "1" );

		if ( formatPlan.isDoingChunking() ) {
			String rootFilename = targetFile.getName();
			rootFilename = rootFilename.substring( 0, rootFilename.lastIndexOf( '.' ) );
			transformer.setParameter( "root.filename", rootFilename );
		}
		return transformer;
	}

	protected Source buildSource(File sourceFile) throws RenderingException {
		// IMPL NOTE : if profiling is enabled, we do not need to perform the value injections
		//		here because they were already applied during profiling...
		return FileUtils.createSAXSource(
				sourceFile,
				entityResolver,
				environment.getOptions().isXincludeSupported(),
				environment.getProfilingConfiguration().isEnabled()
						? null
						: environment.getValueInjections()
		);
    }

	protected Result buildResult(File targetFile) throws RenderingException, XSLTException {
		return new StreamResult( targetFile );
	}

	protected void releaseResult(Result transformationResult) {
		// typically nothing to do...
	}
}
