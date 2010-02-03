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
package org.jboss.jdocbook.render.impl.fop;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.jdocbook.render.impl.XslFoGenerator;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.PublishingSource;
import org.jboss.jdocbook.render.RenderingEnvironment;
import org.jboss.jdocbook.render.format.StandardDocBookFormatDescriptors;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.xslt.XSLTException;
import org.jboss.jdocbook.util.FileUtils;
import org.jboss.jdocbook.util.LocaleUtils;

/**
 * Delegate for creating an intermediate XSL-FO from the docbook source(s).
 * This plugin uses the more efficient SAX-based PDF generation approach,
 * so this output format is completely unecessary; however, it is sometimes
 * very helpful in debugging xml->pdf issues, especially when engaging the FOP
 * team (they will almost always want to see your FO file).
 *
 * @author Steve Ebersole
 */
public class XslFoGeneratorImpl implements XslFoGenerator {
	private final RenderingEnvironment environment;
	private final FormatPlan formatPlan;

	public XslFoGeneratorImpl(RenderingEnvironment environment, FormatPlan formatPlan) {
		this.environment = environment;
		this.formatPlan = formatPlan;
	}

	public void generateXslFo() throws RenderingException, XSLTException {
		final Transformer transformer = buildXslFoTransformer();

		for ( PublishingSource source : environment.getPublishingSources( true ) ) {
			final String sourceFileBasename = FileUtils.basename( source.resolveDocumentFile().getAbsolutePath() );
			final File fo = new File( source.resolveXslFoDirectory(), sourceFileBasename+".fo" );

			String lang = LocaleUtils.render( source.getLocale(), environment.getOptions().getLocaleSeparator() );
			transformer.setParameter( "l10n.gentext.language", lang );

			boolean createFile;
			if ( ! fo.getParentFile().exists() ) {
				boolean created = fo.getParentFile().mkdirs();
				if ( ! created ) {
					throw new RenderingException( "Unable to create FO file directory" );
				}
				createFile = true;
			}
			else {
				createFile = ! fo.exists();
			}
			if ( createFile ) {
				try {
					boolean created = fo.createNewFile();
					if ( ! created ) {
						environment.log().info( "Filesystem indicated problem creating FO file {}", fo );
					}
				}
				catch ( IOException e ) {
					throw new RenderingException( "Unable to create FO file " + fo.toString() );
				}
			}


			try {
				final OutputStream out = new FileOutputStream( fo );
				try {
					File sourceFile = source.resolveDocumentFile();
					Source sourceStream = new StreamSource( sourceFile );
					Result resultStream = new StreamResult( out );

					try {
						transformer.transform( sourceStream, resultStream );
					}
					catch ( TransformerException e ) {
						throw new RenderingException( "Unable to apply FO transformation", e );
					}
				}
				finally {
					try {
						out.close();
					}
					catch ( IOException e ) {
						environment.log().info( "Unable to close output stream {}", fo );
					}
				}
			}
			catch ( FileNotFoundException e ) {
				throw new RenderingException( "Unable to open output stream to FO file", e );
			}


		}
	}

	private Transformer buildXslFoTransformer() {
		final URL transformationStylesheet;
		if ( formatPlan.getStylesheetResource() == null ) {
			transformationStylesheet = environment.getResourceHelper()
					.requireResource( StandardDocBookFormatDescriptors.PDF.getStylesheetResource() );
		}
		else {
			transformationStylesheet = environment.getResourceHelper()
					.requireResource( formatPlan.getStylesheetResource() );
		}

		return environment.getTransformerBuilder().buildTransformer( formatPlan, transformationStylesheet );
	}
}
