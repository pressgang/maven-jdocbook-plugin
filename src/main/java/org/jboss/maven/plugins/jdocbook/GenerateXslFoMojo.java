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
import java.util.Locale;

import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.render.FormatOptions;
import org.jboss.jdocbook.render.RenderingSource;
import org.jboss.jdocbook.util.StandardDocBookFormatMetadata;

/**
 * Mojo to create an intermediate XSL-FO from the docbook source(s).
 *
 * @goal xslfo
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
@SuppressWarnings({ "UnusedDeclaration" })
public class GenerateXslFoMojo extends AbstractDocBookMojo {
	@Override
	protected void process() throws JDocBookProcessException {
		getComponentRegistry().getXslFoGenerator().generateXslFo(
				new RenderingSourceImpl(),
				getPdfFormatOptions()
		);
	}

	public class RenderingSourceImpl implements RenderingSource {
		public Locale getLanguage() {
			return getMasterLanguageLocale();
		}

		public File resolveSourceDocument() {
			return getRootMasterSourceFile();
		}

		public File resolvePublishingBaseDirectory() {
			// n/a
			return null;
		}

		public File getXslFoDirectory() {
			return directoryLayout.getXslFoDirectory();
		}
	}

	public FormatOptions getPdfFormatOptions() {
		FormatOptions formatOptions = getFormatOptions( StandardDocBookFormatMetadata.PDF.getName() );
		if ( formatOptions == null ) {
			throw new JDocBookProcessException( "Unable to locate PDF format options" );
		}
		return formatOptions;
	}
}
