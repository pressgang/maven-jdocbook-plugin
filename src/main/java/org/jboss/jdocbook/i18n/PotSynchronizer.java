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
package org.jboss.jdocbook.i18n;

import org.jboss.jdocbook.JDocBookProcessException;

/**
 * Contract for synchronizing (creating/updating) PortableObjectTemplate (POT)
 * file(s) from a given master source file.
 * <p/>
 * Any XIncludes contained in the master source file are followed and processed
 * as well.
 *
 * @author Steve Ebersole
 */
public interface PotSynchronizer {
	/**
	 * Perform the synchronization on the the POT files pertaining to the given
	 * master DocBook source.
	 *
	 * @throws JDocBookProcessException unable to synchronize POT files
	 */
	public void synchronizePot() throws JDocBookProcessException;

}
