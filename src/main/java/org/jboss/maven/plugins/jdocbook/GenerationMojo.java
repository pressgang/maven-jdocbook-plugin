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

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;
import org.jboss.jdocbook.profile.ProfilerFactory;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.render.RendererFactory;
import org.jboss.jdocbook.xslt.XSLTException;
import org.xml.sax.EntityResolver;

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
public class GenerationMojo extends AbstractDocBookMojo {
	/**
	 * INTERNAL : The artifacts associated with the dependencies defined as part
	 * of the project to which we are being attached.
	 *
	 * @parameter expression="${project.artifacts}"
     * @required
     * @readonly
	 */
	protected Set projectArtifacts;

	/**
	 * INTERNAL : The artifacts associated to the dependencies defined as part
	 * of our configuration within the project to which we are being attached.
	 *
	 * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
	 */
	protected List pluginArtifacts;

	private final ProfilerFactory profilerFactory = new ProfilerFactory( this );
	private final RendererFactory rendererFactory = new RendererFactory( this );

	@SuppressWarnings({ "unchecked" })
	@Override
	protected void process() throws XSLTException, RenderingException {
		if ( !sourceDirectory.exists() ) {
			getLog().info( "sourceDirectory [" + sourceDirectory.getAbsolutePath() + "] did not exist" );
			return;
		}

		if ( options.getDocbookVersion() == null ) {
			List<Artifact> artifacts = new ArrayList<Artifact>();
			artifacts.addAll( projectArtifacts );
			artifacts.addAll( pluginArtifacts );

			for ( Artifact artifact : artifacts ) {
				if ( "net.sf.docbook".equals( artifact.getGroupId() ) &&
						"docbook".equals( artifact.getArtifactId() ) ) {
					getLog().debug( "Found docbook version : " + artifact.getVersion() );
					if ( options.getDocbookVersion() != null ) {
						getLog().warn( "found multiple docbook versions" );
					}
					options.setDocbookVersion( artifact.getVersion() );
				}
			}
		}

		if ( !workDirectory.exists() ) {
			boolean created = workDirectory.mkdirs();
			if ( !created ) {
				loggingBridge.info( "Unable to create work directory {}", workDirectory.getAbsolutePath() );
			}
		}

		if ( profiling.isEnabled() ) {
			profilerFactory.buildProfiler().applyProfiling();
		}

		final EntityResolver entityResolver = getEntityResolver();

		for ( FormatPlan formatPlan : getFormatPlans() ) {
			rendererFactory.buildRenderer( formatPlan, entityResolver ).render();
		}
	}
}
