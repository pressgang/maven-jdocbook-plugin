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

/**
 * Enumeration of the different strategies defined by the DocBook reference manual for applying profiling.
 *
 * @author Steve Ebersole
 */
public class DocBookProfilingStrategy {
	public static final DocBookProfilingStrategy NONE = new DocBookProfilingStrategy( "none" );
	public static final DocBookProfilingStrategy SINGLE_PASS = new DocBookProfilingStrategy( "single_pass" );
	public static final DocBookProfilingStrategy TWO_PASS = new DocBookProfilingStrategy( "two_pass" );

	private final String name;

	public DocBookProfilingStrategy(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static DocBookProfilingStrategy parse(String text) {
		if ( SINGLE_PASS.name.equalsIgnoreCase( text ) ) {
			return SINGLE_PASS;
		}
		else if ( TWO_PASS.name.equalsIgnoreCase( text ) ) {
			return TWO_PASS;
		}
		else {
			// default...
			return NONE;
		}
	}
}
