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

import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.render.format.StandardDocBookFormatDescriptors;
import org.jboss.jdocbook.render.RendererFactory;
import org.xml.sax.EntityResolver;

/**
 * Mojo to create an intermediate XSL-FO from the docbook source(s).
 *
 * @see org.jboss.jdocbook.render.impl.fop.XslFoGeneratorImpl for details.
 *
 * @goal xslfo
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
public class GenerateXslFoMojo extends AbstractDocBookMojo {
	private final RendererFactory rendererFactory = new RendererFactory( this );

	@Override
	protected void process() throws JDocBookProcessException {
		final EntityResolver entityResolver = getEntityResolver();
		final FormatPlan pdfFormatPlan = getFormatPlan( StandardDocBookFormatDescriptors.PDF );
		rendererFactory.buildXslFoGenerator( pdfFormatPlan, entityResolver ).generateXslFo();
	}
}
