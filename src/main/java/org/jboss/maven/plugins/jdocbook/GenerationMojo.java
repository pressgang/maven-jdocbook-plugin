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

import org.jboss.jdocbook.profile.Profiler;
import org.jboss.jdocbook.render.FormatOptions;
import org.jboss.jdocbook.render.Renderer;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.xslt.XSLTException;

/**
 * This mojo's responsibility within the plugin/packaging is actually performing 
 * the DocBook transformations.  At the highest level, it takes the source and
 * process it via the specified DocBook XSLT to produce output.
 *
 * @goal generate
 * @phase compile
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
@SuppressWarnings({ "UnusedDeclaration" })
public class GenerationMojo extends AbstractDocBookMojo {

	@Override
	@SuppressWarnings({ "unchecked" })
	protected void process() throws XSLTException, RenderingException {
		if ( !sourceDirectory.exists() ) {
			getLog().info( "sourceDirectory [" + sourceDirectory.getAbsolutePath() + "] did not exist" );
			return;
		}

		final Profiler profiler = getComponentRegistry().getProfiler();
		final Renderer renderer = getComponentRegistry().getRenderer();

		final Matcher<String> matcher = new Matcher<String>( getRequestedFormat() );

		for ( PublishingSource publishingSource : resolvePublishingSources() ) {
			if ( profiling.isEnabled() ) {
				profiler.profile( publishingSource );
			}
			for ( FormatOptions formatOptions : getFormatOptionsList() ) {
				if ( matcher.matches( formatOptions.getName() ) ) {
					renderer.render( publishingSource, formatOptions );
				}
			}
		}
	}
}
