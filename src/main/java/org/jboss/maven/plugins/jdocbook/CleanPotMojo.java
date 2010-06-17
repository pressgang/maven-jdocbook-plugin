/*
 * jDocBook, processing of DocBook sources
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
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

import java.io.File;
import java.io.IOException;

import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.util.FileUtils;

/**
 * Cleanup the POT files.
 *
 * @goal clean-pot
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
@SuppressWarnings({ "UnusedDeclaration" })
public class CleanPotMojo extends AbstractDocBookMojo {
	protected void doExecute() throws JDocBookProcessException {
		final File potDirectory = directoryLayout.getPotSourceDirectory();
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
