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

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

import org.jboss.jdocbook.Environment;
import org.jboss.jdocbook.JDocBookProcessException;

/**
 * Simple helpers for locating and handling classpath and file URL resource
 * lookups.
 *
 * @author Steve Ebersole
 */
public class ResourceHelper {
	private final ClassLoader combinedClassLoader;

	public ResourceHelper(Environment environment) {
		this.combinedClassLoader = new URLClassLoader(
				extractClasspathUrls( environment ),
				Thread.currentThread().getContextClassLoader()
		);
	}

	private URL[] extractClasspathUrls(Environment environment) {
		final URL[] styleUrls = environment.getClasspathUriResolverBaseUrls();
		final URL[] combined;

		if ( environment.getStagingDirectory() != null ) {
			combined = new URL[ styleUrls.length + 1 ];
			try {
				combined[0] = environment.getStagingDirectory().toURI().toURL();
			}
			catch ( MalformedURLException e ) {
				throw new JDocBookProcessException( "Unable to resolve staging dir to URL", e );
			}
			System.arraycopy( styleUrls, 0, combined, 1, styleUrls.length );
		}
		else {
			combined = styleUrls;
		}
		return combined;
	}

	public ClassLoader getCombinedClassLoader() {
		return combinedClassLoader;
	}

	/**
	 * Locate said resource, throwing an exception if it could not be found.
	 *
	 * @param name The resource name.
	 * @return The resource's URL.
	 * @throws IllegalArgumentException If the resource could not be found.
	 */
	public URL requireResource(String name) {
		URL resource = locateResource( name );
		if ( resource == null ) {
			throw new IllegalArgumentException( "could not locate resource [" + name + "]" );
		}
		return resource;
	}

	/**
	 * Locate said resource.
	 *
	 * @param name The resource name.
	 * @return The resource's URL.
	 */
	public URL locateResource(String name) {
		if ( name.startsWith( "classpath:" ) ) {
			return locateClassPathResource( name.substring( 10 ) );
		}
		else if ( name.startsWith( "file:" ) ) {
			try {
				return new URL( name );
			}
			catch ( MalformedURLException e ) {
				throw new IllegalArgumentException( "malformed explicit file url [" + name + "]");
			}
		}
		else {
			// assume a classpath resource (backwards compatibility)
			return locateClassPathResource( name );
		}
	}

	private URL locateClassPathResource(String name) {
		while ( name.startsWith( "/" ) ) {
			name = name.substring( 1 );
		}

		URL result = combinedClassLoader.getResource( name );
		if ( result == null ) {
			result = combinedClassLoader.getResource( "/" + name );
		}

		return result;
	}

}