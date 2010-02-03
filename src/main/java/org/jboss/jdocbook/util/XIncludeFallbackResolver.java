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

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * EntityResolver implementation for providing <a href="https://fedorahosted.org/publican/">publican</a> style
 * fallback resolution of <a href="http://www.w3.org/TR/xinclude/">XIncludes</a> without the content authors having to
 * manually add the tedious nested <samp>&lt;xi:fallback/&gt;</samp> entries.
 *
 * @author Steve Ebersole
 */
public class XIncludeFallbackResolver implements EntityResolver {
	private final EntityResolver mainResolver;
	private final String baseSourceDirectoryURI;
	private final String baseFallbackURLPath;

	private final int baseSourceDirectoryURILength;

	public XIncludeFallbackResolver(EntityResolver mainResolver, String baseSourceDirectoryPath, String baseFallbackURLPath) {
		this.mainResolver = mainResolver;

		this.baseSourceDirectoryURI = "file:" +  cleanupBaseSourceDirectoryPath( baseSourceDirectoryPath );
		this.baseSourceDirectoryURILength = baseSourceDirectoryURI.length();

		this.baseFallbackURLPath = baseFallbackURLPath.endsWith( "/" )
				? baseFallbackURLPath
				: baseFallbackURLPath + "/";
	}

	private String cleanupBaseSourceDirectoryPath(String path) {
		if ( ! path.startsWith( "/" ) ) {
			path = "/" + path;
		}
		if ( ! path.endsWith( "/" ) ) {
			path += "/";
		}
		return path;
	}

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		InputSource resolvedSource = mainResolver.resolveEntity( publicId, systemId );
		if ( resolvedSource == null ) {
			resolvedSource = resolveFallback( publicId, systemId );
		}
		return resolvedSource;
	}

	protected InputSource resolveFallback(String publicId, String systemId) {
		// Not sure this is necessarily truth, but in my experience XInclude entities come through an EntityResolver
		// such as this with publicId==null and systemId as some file: protocol URI
		if ( publicId != null || ( systemId == null || ! systemId.startsWith( baseSourceDirectoryURI ) ) ) {
			return null;
		}

		String ref = systemId.substring( baseSourceDirectoryURILength );
		String possibleFallback = baseFallbackURLPath + ref;
		try {
			URL url = new URL( possibleFallback );
			URLConnection urlConnection = url.openConnection();
			urlConnection.setDoInput( true );
			urlConnection.setDoOutput( false );
			urlConnection.connect();

			return new InputSource( urlConnection.getInputStream() );
		}
		catch ( MalformedURLException ignore ) {
		}
		catch ( IOException ignore ) {
		}

		return null;
	}
}
