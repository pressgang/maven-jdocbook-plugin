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

import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.translate.TranslationSource;
import org.jboss.jdocbook.translate.Translator;

/**
 * Responsible for translating the DocBook source based on the translator's PO files.
 *
 * @goal translate
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
public class TranslationMojo extends AbstractDocBookMojo {
	@Override
	protected void doExecute() throws JDocBookProcessException {
		final Translator translator = getComponentRegistry().getTranslator();
		for ( TranslationSource source : getTranslationSources( true ) ) {
			translator.translate( source );
		}
	}
}
