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
package org.jboss.jdocbook.render.impl;

import java.io.File;
import javax.xml.transform.Result;

import org.jboss.jdocbook.render.RenderingEnvironment;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.render.impl.fop.ResultImpl;
import org.jboss.jdocbook.render.impl.fop.XslFoGeneratorImpl;
import org.xml.sax.EntityResolver;

/**
 * Implementation of the {@link org.jboss.jdocbook.render.Renderer} contract specifically for dealing with PDF generation.
 *
 * @author Steve Ebersole
 */
public class PDFRenderer extends BasicRenderer {
	public PDFRenderer(RenderingEnvironment environment, EntityResolver entityResolver, FormatPlan formatPlan) {
		super( environment, entityResolver, formatPlan );
	}

	protected Result buildResult(File targetFile) throws RenderingException {
		return new ResultImpl( targetFile, environment );
	}

	protected void releaseResult(Result transformationResult) {
		( ( ResultImpl ) transformationResult ).release();
	}

	public XslFoGenerator buildXslFoGenerator() {
		return new XslFoGeneratorImpl( environment, formatPlan );
	}

}
