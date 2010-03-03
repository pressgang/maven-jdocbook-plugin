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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Arrays;
import java.util.Date;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeResolutionListener;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.Log;
import org.jboss.jdocbook.ValueInjection;
import org.jboss.jdocbook.i18n.Factory;
import org.jboss.jdocbook.i18n.I18nEnvironment;
import org.jboss.jdocbook.i18n.I18nSource;
import org.jboss.jdocbook.i18n.MasterTranslationDescriptor;
import org.jboss.jdocbook.render.PublishingSource;
import org.jboss.jdocbook.render.RenderingEnvironment;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.render.format.StandardDocBookFormatDescriptor;
import org.jboss.jdocbook.render.format.StandardDocBookFormatDescriptors;
import org.jboss.jdocbook.util.LocaleUtils;
import org.jboss.jdocbook.util.ResourceHelper;
import org.jboss.jdocbook.xslt.TransformerBuilder;
import org.jboss.jdocbook.xslt.XSLTException;
import org.jboss.jdocbook.xslt.catalog.ExplicitCatalogManager;
import org.jboss.jdocbook.xslt.catalog.ImplicitCatalogManager;
import org.jboss.jdocbook.xslt.resolve.entity.EntityResolverChain;
import org.jboss.jdocbook.xslt.resolve.entity.LocalDocBookEntityResolver;
import org.jboss.maven.util.logging.PlexusToMavenPluginLoggingBridge;
import org.xml.sax.EntityResolver;

/**
 * Basic support for the various DocBook mojos in this packaging plugin.
 * Mainly, we are defining common configuration attributes of the packaging.
 * 
 * @author Steve Ebersole
 */
public abstract class AbstractDocBookMojo extends AbstractMojo implements RenderingEnvironment, I18nEnvironment {
	public static final String PLUGIN_NAME = "jdocbook";

	/**
	 * INTERNAL : The project being built
	 *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

	/**
	 * INTERNAL : The artifacts associated to the dependencies defined as part
	 * of our configuration within the project to which we are being attached.
	 *
	 * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
	 */
	protected List pluginArtifacts;

	/**
	 * INTERNAL : The representation of the maven execution.
	 *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
	protected MavenSession session;

	/**
	 * INTERNAL : The user settings (used to locate http proxy information).
	 *
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    protected Settings settings;

    /**
     * INTERNAL : The artifact repository to use.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * INTERNAL : The artifact metadata source to use.
     *
     * @component
     * @required
     * @readonly
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * INTERNAL : The artifact collector to use.
     *
     * @component
     * @required
     * @readonly
     */
    protected ArtifactCollector artifactCollector;

    /**
     * INTERNAL : used to get reference to environemtn Archiver/UnArchiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.manager.ArchiverManager}"
     * @required
     * @readonly
     */
    protected ArchiverManager archiverManager;

	/**
	 * INTERNAL : Artifact resolver, needed to download dependencies
	 *
	 * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
	 * @required
	 * @readonly
	 */
	protected ArtifactResolver artifactResolver;

	/**
	 * The name of the document (relative to sourceDirectory) which is the
	 * document to be rendered.
	 *
	 * @parameter
	*  @required
	 */
	protected String sourceDocumentName;

	/**
	 * The directory where the sources are located.
	 *
	 * @parameter expression="${basedir}/src/main/docbook"
	 */
	protected File sourceDirectory;

	/**
	 * A {@link Resource} describing project-local images.
	 *
	 * @parameter
	 */
	protected Resource imageResource;

	/**
	 * A {@link Resource} describing project-local css.
	 *
	 * @parameter
	 */
	protected Resource cssResource;

	/**
	 * The directory containing local fonts
	 *
	 * @parameter expression="${basedir}/src/main/fonts"
	 */
	protected File fontsDirectory;

	/**
	 * The directory where the output will be written.
	 *
	 * @parameter expression="${basedir}/target/docbook/publish"
	 */
	protected File publishDirectory;

	/**
	 * The directory where we can perform some staging staging occurs.  Mainly
	 * this is used for (1) image/css staging; (2) font staging.
	 *
	 * @parameter expression="${basedir}/target/docbook/staging"
	 * @required
	 * @readonly
	 */
	protected File stagingDirectory;

	/**
	 * A directory used for general transient work.
	 *
	 * @parameter expression="${basedir}/target/docbook/work"
	 * @required
	 * @readonly
	 */
	protected File workDirectory;

	/**
	 * The formats in which to perform rendering.
	 *
     * @parameter
	 * @required
	 */
	protected Format[] formats;

	/**
	 * Whether or not to perform the attching of the format
	 * outputs as classified attachments.
	 *
     * @parameter
	 */
	protected boolean attach = true;

	/**
	 * Profiling configuration
	 *
	 * @parameter
	 */
	protected Profiling profiling = new Profiling();

	/**
	 * Configurable options
	 *
     * @parameter
	 */
	protected Options options;

	/**
	 * The injection entities.
	 *
     * @parameter
	 */
	protected Injection[] injections;



	// translation-specific config setting ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Should we ignore translations?  This is useful for temporarily suspending processing of translations from a
	 * profile or other environment specific means.  Note that this setting only affects the docbook processing
	 * phases, not the PO/POT management goals.
	 *
	 * @parameter default-value="false"
	 */
	protected boolean ignoreTranslations;

	/**
	 * The locale of the master translation.
	 *
	 * @parameter default-value="en-US"
	 */
	protected String masterTranslation;

	/**
	 * The locales of all non-master translations.
	 *
	 * @parameter
	 */
	protected String[] translations;

	/**
	 * The directory which contains the translations.  The assumed strategy here is that each translation would
	 * have a directory under the directory named here which would contain the PO sources for that particular
	 * language translation.  The default here is to use sourceDirectory itself.
	 *
	 * @parameter
	 */
	protected File translationBaseDirectory;

	private File resolvedMasterSourceDirectory;

	protected final Factory i18nProcesserFactory = new Factory( this );

	public Factory getI18nProcesserFactory() {
		return i18nProcesserFactory;
	}

	protected final MavenLogBridge loggingBridge = new MavenLogBridge();

	private FormatPlan[] formatPlans;

	public FormatPlan[] getFormatPlans() {
		return formatPlans;
	}

	/**
	 * The override method to perform the actual processing of the
	 * mojo.
	 *
	 * @throws RenderingException Indicates problem performing rendering
	 * @throws XSLTException Indicates problem building or executing XSLT transformer
	 */
	protected void process() throws JDocBookProcessException {
	}

	private void doExecuteWithProxy(Proxy proxy) throws JDocBookProcessException {
		String originalHost = null;
		String originalPort = null;
		String originalUser = null;
		String originalPswd = null;

		// First set up jvm environemnt with the proxy settings (storing the original values for later)
		if ( ! empty( proxy.getHost() ) ) {
			originalHost = System.getProperty( "http.proxyHost" );
			System.setProperty( "http.proxyHost", proxy.getHost() );

			originalPort = System.getProperty( "http.proxyPort" );
			System.setProperty( "http.proxyPort", Integer.toString( proxy.getPort() ) );
		}

		if ( !empty( proxy.getUsername() ) ) {
			originalUser = System.getProperty( "http.proxyUser" );
			System.setProperty( "http.proxyUser", proxy.getUsername() );
		}

		if ( ! empty( proxy.getPassword() ) ) {
			originalPswd = System.getProperty( "http.proxyPassword" );
			System.setProperty( "http.proxyPassword", proxy.getPassword() );
		}

		try {
			// Do the processing
			doExecute();
		}
		finally {
			// Restore the original settings
			if ( ! empty( proxy.getHost() ) ) {
				System.setProperty( "http.proxyHost", originalHost );
				System.setProperty( "http.proxyPort", originalPort );
			}
			if ( !empty( proxy.getUsername() ) ) {
				System.setProperty( "http.proxyUser", originalUser );
			}
			if ( !empty( proxy.getPassword() ) ) {
				System.setProperty( "http.proxyPassword", originalPswd );
			}
		}
	}

	private boolean empty(String string) {
		return string == null || "".equals( string );
	}

	protected void doExecute() throws JDocBookProcessException {
		process();
	}

	public final void execute() throws MojoExecutionException, MojoFailureException {
		if ( options == null ) {
			options = new Options();
		}

		formatPlans = determineFormatPlans();

		if ( translationBaseDirectory == null ) {
			translationBaseDirectory = sourceDirectory;
		}

		if ( translations == null ) {
			translations = new String[0];
		}

		resolvedMasterSourceDirectory = sourceDirectory;
		if ( masterTranslation != null && !"".equals( masterTranslation ) ) {
			resolvedMasterSourceDirectory = new File( resolvedMasterSourceDirectory, masterTranslation );
		}

		try {
			if ( settings.getActiveProxy() != null ) {
				doExecuteWithProxy( settings.getActiveProxy() );
			}
			else {
				doExecute();
			}
		}
		catch ( XSLTException e ) {
			throw new MojoExecutionException( "XSLT problem", e );
		}
		catch ( RenderingException e ) {
			throw new MojoExecutionException( "Rendering problem", e );
		}
		catch ( JDocBookProcessException e ) {
			throw new MojoExecutionException( "Unexpected problem", e );
		}
	}

	private FormatPlan[] determineFormatPlans() {
		final String requestedFormat = getRequestedFormat();
		return requestedFormat != null ? resolveRestrictedFormatPlans( requestedFormat ) : resolveUnrestrictedFormatPlans();
	}

	private FormatPlan[] resolveRestrictedFormatPlans(String requestedFormat) {
		FormatPlan[] plans = new FormatPlan[1];
		for ( Format format : formats ) {
			if ( requestedFormat.equals( format.getFormatName() ) ) {
				plans[0] = buildFormatPlan( format );
			}
		}
		if ( plans[0] == null ) {
			plans = new FormatPlan[0];
		}
		return plans;
	}

	private FormatPlan buildFormatPlan(Format format) {
		return new FormatPlan(
				format,
				StandardDocBookFormatDescriptors.getDescriptor( format.getFormatName() )
		);
	}

	private FormatPlan[] resolveUnrestrictedFormatPlans() {
		FormatPlan[] plans = new FormatPlan[ formats.length ];
		for ( int i = 0; i < formats.length; i++ ) {
			plans[i] = buildFormatPlan( formats[i] );
		}
		return plans;
	}

	protected String getRequestedFormat() {
		String requestedFormat = session.getExecutionProperties().getProperty( "jdocbook.format" );
		if ( requestedFormat != null ) {
			getLog().debug( "requested processing limited to [" + requestedFormat + "] format" );
		}
		return requestedFormat;
	}

	protected Locale getRequestedLocale() {
		String requestedLocaleStr = session.getExecutionProperties().getProperty( "jdocbook.lang" );
		Locale requestedLocale = requestedLocaleStr == null ? null : parseLocale( requestedLocaleStr );
		if ( requestedLocale != null ) {
			getLog().debug( "requested processing limited to [" + stringify( requestedLocale ) + "] lang" ) ;
		}
		return requestedLocale;
	}

	public Log log() {
		return loggingBridge;
	}

	public Options getOptions() {
		return options;
	}

	private List<ValueInjection> valueInjections;

	public List<ValueInjection> getValueInjections() {
		if ( valueInjections == null ) {
			valueInjections = new ArrayList<ValueInjection>();
			if ( injections != null ) {
				valueInjections.addAll( Arrays.asList( injections ) );
			}
			if ( options.isApplyStandardInjectionValues() ) {
				valueInjections.add( new ValueInjection( "version", project.getVersion() ) );
				SimpleDateFormat dateFormat = new SimpleDateFormat( options.getInjectionDateFormat() );
				valueInjections.add( new ValueInjection( "today", dateFormat.format( new Date() ) ) );
			}
		}
		return valueInjections;
	}

	private TransformerBuilder transformerBuilder;

	public TransformerBuilder getTransformerBuilder() {
		if ( transformerBuilder == null ) {
			transformerBuilder = new TransformerBuilder( this );
		}
		return transformerBuilder;
	}

	private EntityResolver entityResolver;

	public EntityResolver getEntityResolver() {
		if ( entityResolver == null ) {
			CatalogManager catalogManager;
			if ( options.getCatalogs() == null || options.getCatalogs().length == 0 ) {
				catalogManager = new ImplicitCatalogManager();
			}
			else {
				catalogManager = new ExplicitCatalogManager( options.getCatalogs() );
			}
			entityResolver = new EntityResolverChain( new CatalogResolver( catalogManager ) );
			( (EntityResolverChain) entityResolver ).addEntityResolver( new LocalDocBookEntityResolver() );
			// todo : wrapping doctype injector per MPJDOCBOOK-50
		}
		return entityResolver;
	}

	private URL[] styleArtifactUrls;

	@SuppressWarnings({ "unchecked" })
	public URL[] getClasspathUriResolverBaseUrls() {
		if ( styleArtifactUrls == null ) {
			// Three sources for resolver base urls:
			List<URL> urlList = new ArrayList<URL>();
			// 		1) staging dir
			if ( getStagingDirectory() != null ) {
				try {
					urlList.add( getStagingDirectory().toURI().toURL() );
				}
				catch ( MalformedURLException e ) {
					getLog().warn( "Uanble to convert staging directory to url" );
				}
			}
			//		2) project dependencies
			for ( Artifact artifact : (Set<Artifact>) project.getArtifacts() ) {
				if ( artifact.getFile() != null ) {
					try {
						urlList.add( artifact.getFile().toURI().toURL() );
					}
					catch ( MalformedURLException e ) {
						getLog().warn( "Uanble to retrieve artifact url [" + artifact.getId() + "]" );
					}
				}
			}
			//		3) plugin dependencies (this should be plugin *injected* dependencies)
			if ( pluginArtifacts != null ) {
				for ( Artifact artifact : (List<Artifact>) pluginArtifacts ) {
					if ( artifact.getFile() != null ) {
						try {
							urlList.add( artifact.getFile().toURI().toURL() );
						}
						catch ( MalformedURLException e ) {
							getLog().warn( "Uanble to retrieve artifact url [" + artifact.getId() + "]" );
						}
					}
				}
			}
			styleArtifactUrls = urlList.toArray( new URL[ urlList.size() ] );
		}
		return styleArtifactUrls;
	}

	private ResourceHelper resourceHelper;

	public ResourceHelper getResourceHelper() {
		if ( resourceHelper == null ) {
			resourceHelper = new ResourceHelper( this );
		}
		return resourceHelper;
	}

	public FormatPlan getFormatPlan(StandardDocBookFormatDescriptor format) {
		for ( FormatPlan plan : formatPlans ) {
			if ( plan.getName().equals( format.getName() ) ) {
				return plan;
			}
		}
		return null;
	}

	public File getStagingDirectory() {
		return stagingDirectory;
	}

	public File getWorkDirectory() {
		return workDirectory;
	}

	public File[] getFontDirectories() {
		List<File> directories = new ArrayList<File>();

		if ( fontsDirectory != null && fontsDirectory.exists() ) {
			directories.add( fontsDirectory );
		}

		File stagedFontsDirectory = new File( getStagingDirectory(), "fonts" );
		if ( stagedFontsDirectory.exists() ) {
			directories.add( stagedFontsDirectory );
		}

		return directories.toArray( new File[ directories.size() ] );
	}

	public Profiling getProfilingConfiguration() {
		return profiling;
	}

	public File getFontStagingDirectory() {
		return new File( getStagingDirectory(), "fonts" );
	}

	public List<PublishingSource> getPublishingSources(boolean excludeIngoredTranslations) {
		Locale requestedLocale = getRequestedLocale();
		boolean requestedLocaleIsTranslation = requestedLocale != null
				&& ! stringify( requestedLocale ).equals( masterTranslation );
		boolean skipAllTranslations = ignoreTranslations && excludeIngoredTranslations
				&& !requestedLocaleIsTranslation;

		ArrayList<PublishingSource> descriptors = new ArrayList<PublishingSource>();
		MasterTranslationDescriptorImpl masterTranslationImpl = new MasterTranslationDescriptorImpl();
		if ( requestedLocale == null || requestedLocale.equals( masterTranslationImpl.getLocale() ) ) {
			descriptors.add( new MasterTranslationDescriptorImpl() );
		}

		if ( skipAllTranslations ) {
			getLog().info( "Skipping all translations" );
		}
		else {
			for ( String localeStr : translations ) {
				final Locale locale = parseLocale( localeStr );
				final boolean skipThisLocale = requestedLocale != null
						&& !requestedLocale.equals( locale )
						&& excludeIngoredTranslations;
				if ( skipThisLocale ) {
					getLog().debug( "skipping non-requested lang [" + localeStr + "]" );
					continue;
				}
				descriptors.add( new OtherTranslationDescriptorImpl( locale ) );
			}
		}

		return descriptors;
	}

	public MasterTranslationDescriptor getMasterTranslationDescriptor() {
		return new MasterTranslationDescriptorImpl();
	}

	public List<I18nSource> getI18nSources(boolean excludeIngoredTranslations) {
		ArrayList<I18nSource> descriptors = new ArrayList<I18nSource>();

		Locale requestedLocale = getRequestedLocale();
		boolean requestedLocaleIsTranslation = requestedLocale != null
				&& ! stringify( requestedLocale ).equals( masterTranslation );

		boolean skipAllTranslations = ignoreTranslations
				&& excludeIngoredTranslations
				&& !requestedLocaleIsTranslation;

		if ( skipAllTranslations ) {
			getLog().info( "Skipping all translations" );
		}
		else {
			for ( String localeStr : translations ) {
				final Locale locale = parseLocale( localeStr );
				final boolean skipThisLocale = requestedLocale != null
						&& !requestedLocale.equals( locale )
						&& excludeIngoredTranslations;
				if ( skipThisLocale ) {
					getLog().debug( "skipping non-requested lang [" + localeStr + "]" );
					continue;
				}
				descriptors.add( new OtherTranslationDescriptorImpl( locale ) );
			}
		}

		return descriptors;
	}

	private class MasterTranslationDescriptorImpl implements PublishingSource, MasterTranslationDescriptor {
		public Locale getLocale() {
			return LocaleUtils.parse( masterTranslation, options.getLocaleSeparator() );
		}

		public File resolveMasterDocument() {
			return new File( resolvedMasterSourceDirectory, sourceDocumentName );
		}

		public File resolvePotDirectory() {
			return new File( translationBaseDirectory, "pot" );
		}

		public FormatPlan[] getFormatPlans() {
			return formatPlans;
		}

		public File resolveDocumentFile() {
			return resolveMasterDocument();
		}

		public File resolvePublishingDirectory() {
			return determinePublishingDirectory( masterTranslation );
		}

		public File resolveXslFoDirectory() {
			return determineXslFoDirectory( masterTranslation );
		}

		public File resolveProfilingDirectory() {
			return determineProfilingDirectory( masterTranslation );
		}

		public File resolveProfiledDocumentFile() {
			return new File( resolveProfilingDirectory(), sourceDocumentName );
		}
	}

	private class OtherTranslationDescriptorImpl implements PublishingSource, I18nSource {
		private final Locale locale;

		private OtherTranslationDescriptorImpl(Locale locale) {
			this.locale = locale;
		}

		public Locale getLocale() {
			return locale;
		}

		public FormatPlan[] getFormatPlans() {
			return formatPlans;
		}

		public File resolveDocumentFile() {
			return new File( determineTranslatedXmlDirectory( stringify( locale ) ), sourceDocumentName );
		}

		public File resolvePublishingDirectory() {
			return determinePublishingDirectory( stringify( locale ) );
		}

		public File resolveXslFoDirectory() {
			return determineXslFoDirectory( stringify( locale ) );
		}

		public File resolveProfilingDirectory() {
			return determineProfilingDirectory( stringify( locale ) );
		}

		public File resolvePoDirectory() {
			return determineTranslationDirectory( stringify( locale ) );
		}

		public File resolveTranslatedXmlDirectory() {
			return determineTranslatedXmlDirectory( stringify( locale ) );
		}

		public File resolveProfiledDocumentFile() {
			return new File( resolveProfilingDirectory(), sourceDocumentName );
		}
	}

	protected File determinePublishingDirectory(String lang) {
		return new File( publishDirectory, lang );
	}

	protected File determineXslFoDirectory(String lang) {
		return new File( new File( workDirectory, "xsl-fo" ), lang );
	}

	protected File determineProfilingDirectory(String lang) {
		return new File( new File( workDirectory, "profile" ), lang );
	}

	protected File determineTranslationDirectory(String lang) {
		return new File( translationBaseDirectory, lang );
	}

	protected File determineTranslatedXmlDirectory(String lang) {
		return new File( new File( workDirectory, "xml" ), lang );
	}


	@SuppressWarnings({ "unchecked" })
	protected List<Artifact> collectArtifactsByType(String type, boolean transitivesFirst) {
		Set dependencyArtifacts = project.getArtifacts();
		dependencyArtifacts.addAll( pluginArtifacts );

		DependencyTreeResolutionListener listener = new DependencyTreeResolutionListener(
				new PlexusToMavenPluginLoggingBridge( getLog() )
		);

		try {
			artifactCollector.collect(
					dependencyArtifacts,
					project.getArtifact(),
					project.getManagedVersionMap(),
					localRepository,
					project.getRemoteArtifactRepositories(),
					artifactMetadataSource,
					null,
					Collections.singletonList( listener )
			);
		}
		catch ( AbstractArtifactResolutionException e ) {
			throw new JDocBookProcessException( "Cannot build project dependency tree", e );
		}

		List<Artifact> artifacts = new ArrayList<Artifact>();
		processNode( listener.getRootNode(), artifacts, type, transitivesFirst );
		return artifacts;
	}

	@SuppressWarnings({ "unchecked" })
	private void processNode(DependencyNode node, List<Artifact> artifacts, String type, boolean transitivesFirst) {
		final Artifact artifact = node.getArtifact();
		final boolean isProjectArtifact = project.getArtifact().getId().equals( artifact.getId() );

		if ( ! isProjectArtifact ) {
			resolveArtifact( artifact );

		}

		if ( ! isProjectArtifact && ! transitivesFirst ) {
			if ( include( node.getArtifact(), type ) ) {
				artifacts.add( node.getArtifact() );
			}
		}

		for ( DependencyNode child : ( List<DependencyNode> ) node.getChildren() ) {
			processNode( child, artifacts, type, transitivesFirst );
		}

		if ( !isProjectArtifact && transitivesFirst ) {
			if ( include( node.getArtifact(), type ) ) {
				artifacts.add( node.getArtifact() );
			}
		}
	}

	private void resolveArtifact(Artifact artifact) {
		try {
			artifactResolver.resolve( artifact, project.getRemoteArtifactRepositories(), localRepository );
		}
		catch ( ArtifactResolutionException e ) {
			throw new JDocBookProcessException( "Unable to resolve artifact [" + artifact.getId() + "]", e );
		}
		catch ( ArtifactNotFoundException e ) {
			throw new JDocBookProcessException( "Unable to locate artifact [" + artifact.getId() + "]", e );
		}
	}

	private boolean include(Artifact artifact, String matchingType) {
		return matchingType.equals( artifact.getType() );
	}

	protected Locale parseLocale(String locale) {
		return LocaleUtils.parse( locale, options.getLocaleSeparator() );
	}

	protected String stringify(Locale locale) {
		return LocaleUtils.render( locale, options.getLocaleSeparator() );
	}

	protected class MavenLogBridge implements Log {
		public void trace(String message) {
			getLog().debug( message );
		}

		public void trace(String message, Object... args) {
			if ( getLog().isDebugEnabled() ) {
				getLog().debug( MessageFormat.format( message, args ) );
			}
		}

		public void info(String message) {
			getLog().info( message );
		}

		public void info(String message, Object... args) {
			if ( getLog().isInfoEnabled() ) {
				getLog().info( MessageFormat.format( message, args ) );
			}
		}

		public void info(String message, Throwable exception) {
			getLog().info( message, exception );
		}

		public void info(String message, Throwable exception, Object... args) {
			if ( getLog().isInfoEnabled() ) {
				getLog().info( MessageFormat.format( message, args ), exception );
			}
		}

		public void error(String message) {
			getLog().error( message );
		}

		public void error(String message, Object... args) {
			if ( getLog().isErrorEnabled() ) {
				getLog().error( MessageFormat.format( message, args ) );
			}
		}

		public void error(String message, Throwable exception) {
			getLog().error( message, exception );
		}

		public void error(String message, Throwable exception, Object... args) {
			if ( getLog().isErrorEnabled() ) {
				getLog().error( MessageFormat.format( message, args ), exception );
			}
		}
	}
}
