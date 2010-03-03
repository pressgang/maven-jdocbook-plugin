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
package org.jboss.jdocbook.xslt;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;

import com.icl.saxon.Controller;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.util.NoOpWriter;
import org.jboss.jdocbook.util.ResourceHelper;
import org.jboss.jdocbook.xslt.resolve.ClasspathResolver;
import org.jboss.jdocbook.xslt.resolve.CurrentVersionResolver;
import org.jboss.jdocbook.xslt.resolve.ExplicitUrnResolver;
import org.jboss.jdocbook.xslt.resolve.RelativeJarUriResolver;
import org.jboss.jdocbook.xslt.resolve.ResolverChain;
import org.jboss.jdocbook.xslt.resolve.VersionResolver;
import org.jboss.jdocbook.Environment;
import org.jboss.jdocbook.Options;

/**
 * A builder of {@link javax.xml.transform.Transformer} instances, configurable
 * to return either SAXON or XALAN based transformers.
 *
 * @author Steve Ebersole
 */
public class TransformerBuilder {
	private final Environment environment;

	public TransformerBuilder(Environment environment) {
		this.environment = environment;
	}

	private Options options() {
		return environment.getOptions();
	}

	private ResourceHelper resourceHelper() {
		return environment.getResourceHelper();
	}

	public Transformer buildStandardTransformer(URL xslt) {
		URIResolver uriResolver = buildStandardUriResolver();
		return buildTransformer( xslt, uriResolver );
	}

	public Transformer buildStandardTransformer(String xsltResource) {
		URIResolver uriResolver = buildStandardUriResolver();
		return buildTransformer( resourceHelper().requireResource( xsltResource ), uriResolver );
	}

	public Transformer buildTransformer(FormatPlan formatPlan, URL customStylesheet) throws XSLTException {
		URIResolver uriResolver = buildUriResolver( formatPlan );
		URL xsltStylesheet = customStylesheet == null
				? resourceHelper().requireResource( formatPlan.getStylesheetResource() )
				: customStylesheet;
		return buildTransformer( xsltStylesheet, uriResolver );
	}

	private HashMap<String, Templates> transformerTemplatesCache = new HashMap<String, Templates>();

	protected Transformer buildTransformer(URL xslt, URIResolver uriResolver) throws XSLTException {
		javax.xml.transform.TransformerFactory transformerFactory = options().resolveXmlTransformerType().getSAXTransformerFactory();
		transformerFactory.setURIResolver( uriResolver );

		final String xsltUrlStr = xslt.toExternalForm();

		Transformer transformer;
		try {
			Templates transformerTemplates = transformerTemplatesCache.get( xsltUrlStr );
			if ( transformerTemplates == null ) {
				Source source = new StreamSource( xslt.openStream(), xsltUrlStr );
				transformerTemplates = transformerFactory.newTemplates( source );
				transformerTemplatesCache.put( xsltUrlStr, transformerTemplates );
			}
			transformer = transformerTemplates.newTransformer();
//			Source source = new StreamSource( xslt.openStream(), xsltUrlStr );
//			transformer = transformerFactory.newTransformer( source );
		}
		catch ( IOException e ) {
			throw new XSLTException( "problem opening stylesheet [" + xsltUrlStr + "]", e );
		}
		catch ( TransformerConfigurationException e ) {
			throw new XSLTException( "unable to build transformer [" + e.getLocationAsString() + "] : " + e.getMessage(), e );
		}

		configureTransformer( transformer, uriResolver, options().getTransformerParameters() );

		return transformer;

	}

	public void configureTransformer(Transformer transformer, FormatPlan formatPlan) {
		configureTransformer( transformer, buildUriResolver( formatPlan ), options().getTransformerParameters() );
	}

	public static void configureTransformer(Transformer transformer, URIResolver uriResolver, Properties transformerParameters) {
		if ( transformer instanceof Controller ) {
			Controller controller = ( Controller ) transformer;
			try {
				controller.makeMessageEmitter();
				controller.getMessageEmitter().setWriter( new NoOpWriter() );
			}
			catch ( TransformerException te ) {
				// intentionally empty
			}
		}

		transformer.setURIResolver( uriResolver );
		transformer.setParameter( "fop.extensions", "0" );
		transformer.setParameter( "fop1.extensions", "1" );

		if ( transformerParameters == null ) {
			return;
		}
		for ( Map.Entry<Object, Object> entry : transformerParameters.entrySet() ) {
			transformer.setParameter( ( String ) entry.getKey(), entry.getValue() );
		}
	}

	public ResolverChain buildStandardUriResolver() {
		ResolverChain resolverChain = new ResolverChain();
		applyStandardResolvers( resolverChain );
		return resolverChain;
	}

	public ResolverChain buildUriResolver(FormatPlan formatPlan) throws XSLTException {
		return buildUriResolver( formatPlan.getName(), formatPlan.getCorrespondingDocBookStylesheetResource() );
	}

	public ResolverChain buildUriResolver(String formatName, String docBookstyleSheet) throws XSLTException {
		ResolverChain resolverChain = new ResolverChain( new ExplicitUrnResolver( environment, formatName, docBookstyleSheet ) );
		applyStandardResolvers( resolverChain );
		return resolverChain;
	}

	private void applyStandardResolvers(ResolverChain resolverChain) {
		// See https://jira.jboss.org/jira/browse/MPJDOCBOOK-49
		resolverChain.addResolver( new CurrentVersionResolver( environment ) );
		if ( options().getDocbookVersion() != null ) {
			resolverChain.addResolver( new VersionResolver( environment, options().getDocbookVersion() ) );
		}
		resolverChain.addResolver( new RelativeJarUriResolver() );
		resolverChain.addResolver( new ClasspathResolver( resourceHelper().getCombinedClassLoader() ) );
		resolverChain.addResolver( environment.getCatalogResolver() );
	}
}