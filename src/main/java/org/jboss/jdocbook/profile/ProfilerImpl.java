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
package org.jboss.jdocbook.profile;

import java.io.File;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.RenderingEnvironment;
import org.jboss.jdocbook.render.PublishingSource;
import org.jboss.jdocbook.util.Constants;
import org.jboss.jdocbook.util.FileUtils;
import org.jboss.jdocbook.util.LocaleUtils;
import org.jboss.jdocbook.xslt.XSLTException;

/**
 * Implementation of the {@link Profiler} contract.
 *
 * @author Steve Ebersole
 */
class ProfilerImpl implements Profiler {
	private final RenderingEnvironment environment;

	public ProfilerImpl(RenderingEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * {@inheritDoc}
	 */
	public void applyProfiling() {
		try {
			for ( PublishingSource source : environment.getPublishingSources( true ) ) {
				final File targetFile = source.resolveProfiledDocumentFile();
				if ( ! targetFile.getParentFile().exists() ) {
					boolean created = targetFile.getParentFile().mkdirs();
					if ( !created ) {
						environment.log().info( "Unable to create parent directory " + targetFile.getAbsolutePath() );
					}
				}
				environment.log().info( "applying DocBook profiling [" + targetFile.getAbsolutePath() + "]" );

				Transformer xslt = environment.getTransformerBuilder()
						.buildStandardTransformer( Constants.MAIN_PROFILE_XSL_RESOURCE );

				final String lang = LocaleUtils.render( source.getLocale(), environment.getOptions().getLocaleSeparator() );

				xslt.setParameter( "l10n.gentext.language", lang );

				// figure out the attribute upon which to profile
				final String profilingAttributeName = environment.getProfilingConfiguration().getAttributeName();
				if ( profilingAttributeName == null || "lang".equals( profilingAttributeName ) ) {
					xslt.setParameter( "profile.attribute", "lang" );
					xslt.setParameter( "profile.lang", lang );
				}
				else {
					xslt.setParameter( "profile.attribute", profilingAttributeName );
					xslt.setParameter( "profile.value", environment.getProfilingConfiguration().getAttributeValue() );
				}

				xslt.transform( buildSource( source.resolveDocumentFile() ), buildResult( targetFile ) );
			}
		}
		catch ( TransformerException e ) {
			throw new XSLTException( "error performing translation [" + e.getLocationAsString() + "] : " + e.getMessage(), e );
		}
	}

	private Source buildSource(File sourceFile) throws RenderingException {
		return FileUtils.createSAXSource(
				sourceFile,
				environment.getCatalogResolver(),
				environment.getOptions().isXincludeSupported(),
				environment.getValueInjections()
		);
	}

	protected Result buildResult(File targetFile) throws RenderingException, XSLTException {
		return new StreamResult( targetFile );
	}
}
