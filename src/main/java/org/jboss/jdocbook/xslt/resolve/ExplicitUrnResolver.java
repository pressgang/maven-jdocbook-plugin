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
package org.jboss.jdocbook.xslt.resolve;

import java.io.IOException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.jboss.jdocbook.xslt.XSLTException;
import org.jboss.jdocbook.Environment;


/**
 * Resolves an explicit <tt>urn:docbook:stylesheet</tt> URN against the standard
 * DocBook stylesheets.
 *
 * @author Steve Ebersole
 */
public class ExplicitUrnResolver extends BasicUrnResolver {
	private final String name;

	public ExplicitUrnResolver(Environment environment, String name, String stylesheetResource) throws XSLTException {
		super( environment, "urn:docbook:stylesheet", createSource( environment, name, stylesheetResource ) );
		this.name = name;
	}

	private static Source createSource(Environment environment, String name, String stylesheetResource) throws XSLTException {
		URL stylesheet = environment.getResourceHelper().requireResource( stylesheetResource );
		try {
			return new StreamSource( stylesheet.openStream(), stylesheet.toExternalForm() );
		}
		catch ( IOException e ) {
			throw new XSLTException( "could not locate DocBook stylesheet [" + name + "]", e );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return super.toString() + " [" + name + "]";
	}
}