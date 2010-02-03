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

import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.jboss.jdocbook.Environment;

/**
 * Basic support for URIResolvers which map a URN unto a single replacement
 * {@link javax.xml.transform.Source}.
 *
 * @author Steve Ebersole
 */
public class BasicUrnResolver implements URIResolver {
	private final Environment environment;
	private final String urn;
	private final Source source;

	/**
	 * Constructs a {@link URIResolver} which maps occurences of the given <tt>urn</tt> onto the given
	 * <tt>source</tt>
	 *
	 * @param environment The execution environment
	 * @param urn The urn to be replaced.
	 * @param source The value to return instead of the urn.
	 */
	public BasicUrnResolver(Environment environment, String urn, Source source) {
		this.environment = environment;
		this.urn = urn;
		this.source = source;
	}

	/**
	 * {@inheritDoc}
	 */
	public Source resolve(String href, String base) throws TransformerException {
		return urn.equals( href ) ? source : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return super.toString() + " [URN:" + urn + "]";
	}

	protected Environment getEnvironment() {
		return environment;
	}
}