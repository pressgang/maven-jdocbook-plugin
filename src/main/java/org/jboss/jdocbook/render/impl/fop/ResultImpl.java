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

import java.io.OutputStream;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.transform.sax.SAXResult;

import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.RenderingEnvironment;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;

/**
 * {@link SAXResult} object used to pipe PDF XSLT events through to FOP for PDF generation.
*
* @author Steve Ebersole
*/
public class ResultImpl extends SAXResult {
	private OutputStream outputStream;

	public ResultImpl(File targetFile, RenderingEnvironment environment) throws RenderingException {
		try {
			outputStream = new BufferedOutputStream( new FileOutputStream( targetFile ) );

			FopFactory fopFactory = FopFactory.newInstance();
			fopFactory.setUserConfig( FopConfigHelper.getFopConfiguration( environment ) );

			FOUserAgent fopUserAgent = fopFactory.newFOUserAgent();
			fopUserAgent.setProducer( "jDocBook Plugin for Maven" );

			Fop fop = fopFactory.newFop( MimeConstants.MIME_PDF, fopUserAgent, outputStream );
			this.setHandler( fop.getDefaultHandler() );
		}
		catch ( Throwable t ) {
			throw new RenderingException( "error building transformation result [" + targetFile.getAbsolutePath() + "]", t );
		}
	}

	public void release() {
		if ( outputStream == null ) {
			return;
		}
		try {
			outputStream.flush();
			outputStream.close();
		}
		catch ( IOException ignore ) {
		}
	}
}
