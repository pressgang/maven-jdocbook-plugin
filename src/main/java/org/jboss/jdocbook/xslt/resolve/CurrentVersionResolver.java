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

import org.jboss.jdocbook.Environment;

/**
 * Map hrefs starting with <tt>http://docbook.sourceforge.net/release/xsl/current/</tt>
 * to classpath resource lookups.
 *
 * @author Steve Ebersole
 */
public class CurrentVersionResolver extends VersionResolver {
	/**
	 * Constructs a new CurrentVersionResolver instance.
	 *
	 * @param environment The execution environment
	 */
	public CurrentVersionResolver(Environment environment) {
		super( environment, "current" );
	}
}