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
package org.jboss.jdocbook.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.jboss.jdocbook.JDocBookProcessException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A helper for dealing with XIncludes.
 *
 * @author Steve Ebersole
 */
public class XIncludeHelper {
	/**
	 * Given a file which defining an XML document containing XInclude elements, collect all the referenced XInclude
	 * files.
	 *
	 * @param root The file which (potentially) contains XIncludes.
	 * @return The set of files references via XIncludes.
	 */
	public static Set<File> locateInclusions(File root) {
		final Set<File> includes = new TreeSet<File>();

		EntityResolver entityResolver = new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
				if ( publicId == null && systemId != null && systemId.startsWith( "file:/" ) ) {
					try {
						includes.add( new File( new URL( systemId ).getFile() ) );
					}
					catch ( MalformedURLException e ) {
						// should never happen...
						throw new JDocBookProcessException( "Unable to convert reported XInclude href into URL instance [" + systemId + "]" );
					}
				}
				return null;
			}
		};

		try {
			SAXParserFactory parserFactory = new SAXParserFactoryImpl();
    	    parserFactory.setXIncludeAware( true  );

			Source transformationSource = FileUtils.createSAXSource( root, entityResolver, true, null );
			Result transformationResult = new StreamResult( new NoOpWriter() );

			javax.xml.transform.TransformerFactory transformerFactory = new com.icl.saxon.TransformerFactoryImpl();
			transformerFactory.newTransformer().transform( transformationSource, transformationResult );
		}
		catch ( TransformerException e ) {
			throw new JDocBookProcessException( "Problem performing 'transformation'", e );
		}

		return includes;
	}
}