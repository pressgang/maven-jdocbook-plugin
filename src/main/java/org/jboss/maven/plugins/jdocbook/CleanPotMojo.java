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

package org.jboss.maven.plugins.jdocbook;

import java.io.IOException;
import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.jboss.jdocbook.JDocBookProcessException;

/**
 * Cleanup the POT files.
 *
 * @goal clean-pot
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
public class CleanPotMojo extends AbstractDocBookMojo {
	protected void doExecute() throws JDocBookProcessException {
		final File potDirectory = getMasterTranslationDescriptor().resolvePotDirectory();
		if ( potDirectory.exists() ) {
			try {
				FileUtils.cleanDirectory( potDirectory );
			}
			catch ( IOException e ) {
				getLog().warn( "unable to cleanup POT directory [" + potDirectory + "]", e );
			}
		}
	}
}
