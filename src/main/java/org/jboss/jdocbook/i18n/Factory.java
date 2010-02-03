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

import org.jboss.jdocbook.i18n.gettext.PoSynchronizerImpl;
import org.jboss.jdocbook.i18n.gettext.PotSynchronizerImpl;
import org.jboss.jdocbook.i18n.gettext.TranslationBuilderImpl;

/**
 * Factory for various i18n components.  Statically bound to "gettext" and "po/xml" executables.
 *
 * @author Steve Ebersole
 */
public class Factory {
	private final PotSynchronizer potSynchronizer;
	private final PoSynchronizer poSynchronizer;
	private final TranslationBuilder translationBuilder;

	public Factory(I18nEnvironment environment) {
		this.potSynchronizer = new PotSynchronizerImpl( environment );
		this.poSynchronizer = new PoSynchronizerImpl( environment );
		this.translationBuilder = new TranslationBuilderImpl( environment );
	}

	public PotSynchronizer getPotSynchronizer() {
		return potSynchronizer;
	}

	public PoSynchronizer getPoSynchronizer() {
		return poSynchronizer;
	}

	public TranslationBuilder getTranslationBuilder() {
		return translationBuilder;
	}
}
