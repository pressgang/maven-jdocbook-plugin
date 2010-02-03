/*
 * jDocBook, processing of DocBook sources as a Maven plugin
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
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

import org.jboss.jdocbook.render.format.StandardDocBookFormatDescriptors;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.render.impl.PDFRenderer;
import org.jboss.jdocbook.render.impl.BasicRenderer;
import org.jboss.jdocbook.render.impl.XslFoGenerator;
import org.xml.sax.EntityResolver;

/**
 * A factory for building {@link Renderer} instances.
 *
 * @author Steve Ebersole
 */
public class RendererFactory {
	private final RenderingEnvironment environment;

	public RendererFactory(RenderingEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * Build an appropriate renderer for the given <tt>formatName</tt>
	 *
	 * @param formatPlan The format plan
	 * @param entityResolver The entity resolver to use within the renderer.
	 *
	 * @return The renderer.
	 */
	public Renderer buildRenderer(FormatPlan formatPlan, EntityResolver entityResolver) {
		if ( formatPlan.getName().equals( StandardDocBookFormatDescriptors.PDF.getName() ) ) {
			return new PDFRenderer( environment, entityResolver, formatPlan );
		}
		else {
			return new BasicRenderer( environment, entityResolver, formatPlan );
		}
	}
	
	public XslFoGenerator buildXslFoGenerator(FormatPlan formatPlan, EntityResolver entityResolver) {
		return new PDFRenderer( environment, entityResolver, formatPlan ).buildXslFoGenerator();
	}
}
