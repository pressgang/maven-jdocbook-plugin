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

import java.util.Locale;
import java.io.File;

/**
 * Describes a source of localized content for a language.
 *
 * @author Steve Ebersole
 */
public interface I18nSource {
	/**
	 * Retrieve the locale representation of the tralsnation language.
	 *
	 * @return The translation language locale.
	 */
	public Locale getLocale();

	/**
	 * Retrieve the directory containing PO files for this translation.
	 *
	 * @return This translation's PO file directory.
	 */
	public File resolvePoDirectory();

	/**
	 * Retrieve the directory to which translated XML files should go (created by applying the PO files on top of the
	 * master XML).
	 *
	 * @return This translation's XML directory.
	 */
	public File resolveTranslatedXmlDirectory();
}
