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
package org.jboss.jdocbook.render;

import java.util.Locale;
import java.io.File;

import org.jboss.jdocbook.render.format.FormatPlan;

/**
 * Describes a source content DocBook publishing.
 *
 * @author Steve Ebersole
 */
public interface PublishingSource {
	/**
	 * Retrieve the language for this source.
	 *
	 * @return The language for this source.
	 */
	public Locale getLocale();

	public FormatPlan[] getFormatPlans();

	/**
	 * Retrieve the DocBook XML document file  to be processed.
	 *
	 * @return The DocBook XML document file.
	 */
	public File resolveDocumentFile();

	/**
	 * Retrieve the directory to use in order to apply DocBook profilng to this particular publishing source.
	 *
	 * @return The DocBook profiling output directory for this source.
	 */
	public File resolveProfilingDirectory();

	/**
	 * Retrieve the DocBook XML document file  to be processed.
	 *
	 * @return The DocBook XML document file.
	 */
	public File resolveProfiledDocumentFile();

	/**
	 * Retrieve the base directory into which the formatted DocBook outputs should be written.
	 *
	 * @return The base directory into which the formatted DocBook outputs should be written.
	 */
	public File resolvePublishingDirectory();

	public File resolveXslFoDirectory();
}
