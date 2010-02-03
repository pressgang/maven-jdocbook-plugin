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
package org.jboss.jdocbook;

import java.net.URL;
import java.io.File;
import java.util.List;

import org.jboss.jdocbook.xslt.TransformerBuilder;
import org.jboss.jdocbook.util.ResourceHelper;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.render.format.StandardDocBookFormatDescriptor;
import org.apache.xml.resolver.tools.CatalogResolver;

/**
 * Basic environment in which jDocBook is executing.
 *
 * @author Steve Ebersole
 */
public interface Environment {
	/**
	 * Retrieve the logging bridge to the environment's logging system.
	 * 
	 * @return The environment's logging bridge.
	 */
	public Log log();

	/**
	 * Retrieve the user defined configuration options.
	 *
	 * @return The user defined configuration options.
	 */
	public Options getOptions();

	public List<ValueInjection> getValueInjections();

	/**
	 * Get the transformer builder for this environment..
	 *
	 * @return The transformer builder.
	 */
	public TransformerBuilder getTransformerBuilder();

	/**
	 * Retrieve the catalog resolver for this environment.
	 *
	 * @return The catalog resolver.
	 */
	public CatalogResolver getCatalogResolver();

	public URL[] getClasspathUriResolverBaseUrls();

	public File getStagingDirectory();

	public File getWorkDirectory();

	public ResourceHelper getResourceHelper();

	public FormatPlan getFormatPlan(StandardDocBookFormatDescriptor format);
}
