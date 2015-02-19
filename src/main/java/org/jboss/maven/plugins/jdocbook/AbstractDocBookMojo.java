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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Proxy;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeResolutionListener;
import org.jboss.jdocbook.Configuration;
import org.jboss.jdocbook.DocBookSchemaResolutionStrategy;
import org.jboss.jdocbook.Environment;
import org.jboss.jdocbook.JDocBookComponentRegistry;
import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.MasterLanguageDescriptor;
import org.jboss.jdocbook.ResourceDelegate;
import org.jboss.jdocbook.ValueInjection;
import org.jboss.jdocbook.profile.ProfilingSource;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.RenderingSource;
import org.jboss.jdocbook.translate.TranslationSource;
import org.jboss.jdocbook.util.ResourceDelegateSupport;
import org.jboss.jdocbook.util.TranslationUtils;
import org.jboss.jdocbook.util.XIncludeHelper;
import org.jboss.jdocbook.xslt.XSLTException;
import org.jboss.maven.util.logging.PlexusToMavenPluginLoggingBridge;

/**
 * Basic support for the various DocBook mojos in this packaging plugin.
 * Mainly, we are defining common configuration attributes of the packaging.
 * 
 * @author Steve Ebersole
 */
public abstract class AbstractDocBookMojo extends MojoInternalConfigSupport implements DirectoryLayout.BaseInfo {
	public static final String PLUGIN_NAME = "jdocbook";

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
	 * The base directory where output will be written.
	 *
	 * @parameter expression="${basedir}/target"
	 */
	protected File baseOutputDirectory;

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
	 * The formats in which to perform rendering.
	 *
     * @parameter
	 * @required
	 */
	protected Format[] formats;

	/**
	 * Whether or not to perform the attaching of the format
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


	// directory layout ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	protected final DirectoryLayout directoryLayout = new DirectoryLayout( this );

	public File getBaseSourceDirectory() {
		return sourceDirectory;
	}

	public File getBaseOutputDirectory() {
		return baseOutputDirectory;
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

	/**
	 * {@inheritDoc}
	 */
	public final void execute() throws MojoExecutionException, MojoFailureException {
		if ( options == null ) {
			options = new Options();
		}

		if ( translations == null ) {
			translations = new String[0];
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

	private void doExecuteWithProxy(Proxy proxy) throws JDocBookProcessException {
		String originalHost = null;
		String originalPort = null;
		String originalUser = null;
		String originalPswd = null;

		// First set up jvm environment with the proxy settings (storing the original values for later)
		if ( ! empty( proxy.getHost() ) ) {
			originalHost = System.getProperty( "http.proxyHost" );
			System.setProperty( "http.proxyHost", proxy.getHost() );

			originalPort = System.getProperty( "http.proxyPort" );
			System.setProperty( "http.proxyPort", Integer.toString( proxy.getPort() ) );
		}

		if ( !empty( proxy.getUsername() ) ) {
			originalUser = System.getProperty( "http.proxyUser" );
			System.setProperty( "http.proxyUser", emptyStringIfNull( proxy.getUsername() ) );
		}

		if ( ! empty( proxy.getPassword() ) ) {
			originalPswd = System.getProperty( "http.proxyPassword" );
			System.setProperty( "http.proxyPassword", emptyStringIfNull( proxy.getPassword() ) );
		}

		try {
			// Do the processing
			doExecute();
		}
		finally {
			// Restore the original settings
			if ( ! empty( proxy.getHost() ) ) {
				System.setProperty( "http.proxyHost", emptyStringIfNull( originalHost ) );
				System.setProperty( "http.proxyPort", emptyStringIfNull( originalPort ) );
			}
			if ( !empty( proxy.getUsername() ) ) {
				System.setProperty( "http.proxyUser", emptyStringIfNull( originalUser ) );
			}
			if ( !empty( proxy.getPassword() ) ) {
				System.setProperty( "http.proxyPassword", emptyStringIfNull( originalPswd ) );
			}
		}
	}

	private boolean empty(String string) {
		return string == null || "".equals( string );
	}

	private String emptyStringIfNull(String string) {
		// lovely... 1.6 no longer allows the value in System.setProperty to be null
		return string == null ? "" : string;
	}

	protected void doExecute() throws JDocBookProcessException {
		process();
	}

	protected String getRequestedFormat() {
		String requestedFormat = session.getExecutionProperties().getProperty( "jdocbook.format" );
		if ( requestedFormat != null ) {
			getLog().debug( "requested processing limited to [" + requestedFormat + "] format" );
		}
		return requestedFormat;
	}

	protected Locale getRequestedLanguageLocale() {
		String requestedLocaleStr = session.getExecutionProperties().getProperty( "jdocbook.lang" );
		Locale requestedLocale = requestedLocaleStr == null ? null : parseLocale( requestedLocaleStr );
		if ( requestedLocale != null ) {
			getLog().debug( "requested processing limited to [" + stringify( requestedLocale ) + "] lang" ) ;
		}
		return requestedLocale;
	}

	public File[] getFontDirectories() {
		List<File> directories = new ArrayList<File>();

		if ( fontsDirectory != null && fontsDirectory.exists() ) {
			directories.add( fontsDirectory );
		}

		File stagedFontsDirectory = new File( directoryLayout.getStagingDirectory(), "fonts" );
		if ( stagedFontsDirectory.exists() ) {
			directories.add( stagedFontsDirectory );
		}

		return directories.toArray( new File[ directories.size() ] );
	}

	@SuppressWarnings({ "unchecked" })
	protected List<Artifact> collectArtifactsByType(String type, boolean transitivesFirst) {
		Set<Artifact> dependencyArtifacts = project.getArtifacts();
		dependencyArtifacts.addAll( pluginArtifacts );

		DependencyTreeResolutionListener listener = new DependencyTreeResolutionListener(
				new PlexusToMavenPluginLoggingBridge( getLog() )
		);

        artifactCollector.collect(
                dependencyArtifacts,
                project.getArtifact(),
                project.getManagedVersionMap(),
                localRepository,
                project.getRemoteArtifactRepositories(),
                artifactMetadataSource,
                null,
                Collections.singletonList( (ResolutionListener) listener )
        );

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
        // TODO Workaround HACK for https://github.com/pressgang/jdocbook-core/issues/13
        // See also TODO in pom.xml
        if (artifact.getType().equals("jar")
                && (artifact.getGroupId().equals("net.sf.docbook") && (artifact.getArtifactId().equals("docbook-xml")
                && artifact.getVersion().equals("5.0"))
                || (artifact.getGroupId().equals("net.sf.docbook") && artifact.getArtifactId().equals("docbook-xsl")
                && artifact.getVersion().equals("1.76.1")))) {
            return;
        }
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
		return TranslationUtils.parse( locale, options.getLocaleSeparator() );
	}

	protected String stringify(Locale locale) {
		return TranslationUtils.render( locale, options.getLocaleSeparator() );
	}

	public String getMasterLanguage() {
		return masterTranslation;
	}

	private Locale masterLanguageLocale;

	protected Locale getMasterLanguageLocale() {
		if ( masterLanguageLocale == null ) {
			masterLanguageLocale = fromLanguageString( getMasterLanguage() );
		}
		return masterLanguageLocale;
	}

	private boolean isMasterLanguage(String language) {
		return getMasterLanguage().equals( language );
	}

	private boolean isMasterLanguage(Locale language) {
		return getMasterLanguageLocale().equals( language );
	}

	private File rootMasterSourceFile;

	protected File getRootMasterSourceFile() {
		if ( rootMasterSourceFile == null ) {
			rootMasterSourceFile = new File( directoryLayout.getMasterSourceDirectory(), sourceDocumentName );
		}
		return rootMasterSourceFile;
	}

	protected File getSourceDocument(Locale languageLocale) {
		return getSourceDocument( stringify( languageLocale ) );
	}

	protected File getSourceDocument(String language) {
		return isMasterLanguage( language )
				? getRootMasterSourceFile()
				: new File( directoryLayout.getTranslationDirectory( language ), sourceDocumentName );
	}

	protected File getProfiledDocument(Locale languageLocale) {
		return getProfiledDocument( stringify( languageLocale ) );
	}

	protected File getProfiledDocument(String language) {
		return new File( directoryLayout.getProfilingDirectory( language ), sourceDocumentName );
	}

	protected List<Format> getFormatOptionsList() {
		return Arrays.asList( formats );
	}

	protected Format getFormatOptions(String name) {
		for ( Format format : formats ) {
			if ( name.equals( format.getName() ) ) {
				return format;
			}
		}
		return null;
	}

	protected List<PublishingSource> resolvePublishingSources() {
		List<PublishingSource> sources = new ArrayList<PublishingSource>();

		if ( ignoreTranslations ) {
			getLog().info( "Skipping all translations" );
			sources.add( new PublishingSource( getMasterLanguageLocale() ) );
		}
		else {
			Matcher<Locale> matcher = new Matcher<Locale>( getRequestedLanguageLocale() );

			if ( matcher.matches( getMasterLanguageLocale() ) ) {
				sources.add( new PublishingSource( getMasterLanguageLocale() ) );
			}
			else {
				getLog().debug( "skipping master language" );
			}

			for ( String language : translations ) {
				final Locale languageLocale = fromLanguageString( language );
				if ( matcher.matches( languageLocale ) ) {
					sources.add( new PublishingSource( languageLocale ) );
				}
				else {
					getLog().debug( "skipping language " + language );
				}
			}
		}

		return sources;
	}

	protected class PublishingSource implements ProfilingSource, RenderingSource {
		private final Locale languageLocale;

		public PublishingSource(Locale languageLocale) {
			this.languageLocale = languageLocale;
		}


		// ProfilingSource impl ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		public Locale getLanguage() {
			return languageLocale;
		}

		public File resolveDocumentFile() {
			return getSourceDocument( getLanguage() );
		}

		public File resolveProfiledDocumentFile() {
			return getProfiledDocument( languageLocale );
		}


		// RenderingSource impl ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		public File resolveSourceDocument() {
			return profiling.isEnabled()
					? getProfiledDocument( languageLocale )
					: getSourceDocument( languageLocale );
		}

		public File resolvePublishingBaseDirectory() {
			return directoryLayout.getPublishBaseDirectory( stringify( languageLocale ) );
		}

		public File getXslFoDirectory() {
			// n/a
			return null;
		}
	}

	private JDocBookComponentRegistry jDocBookComponentRegistry;

	protected JDocBookComponentRegistry getComponentRegistry() {
		if ( jDocBookComponentRegistry == null ) {
			jDocBookComponentRegistry = buildComponentRegistry();
		}
		return jDocBookComponentRegistry;
	}

	private JDocBookComponentRegistry buildComponentRegistry() {
		return new JDocBookComponentRegistry( new EnvironmentImpl(), new ConfigurationImpl() );
	}

	private class EnvironmentImpl implements Environment {
		private final ResourceDelegateImpl resourceDelegate = new ResourceDelegateImpl();

		public ResourceDelegate getResourceDelegate() {
			return resourceDelegate;
		}

		public MasterLanguageDescriptor getMasterLanguageDescriptor() {
			return AbstractDocBookMojo.this.getMasterLanguageDescriptor();
		}

		public File getWorkDirectory() {
			return directoryLayout.getRootJDocBookWorkDirectory();
		}

		public File getStagingDirectory() {
			return directoryLayout.getStagingDirectory();
		}

		public List<File> getFontDirectories() {
			return Arrays.asList( AbstractDocBookMojo.this.getFontDirectories() );
		}

		public DocBookXsltResolutionStrategy getDocBookXsltResolutionStrategy() {
			return DocBookXsltResolutionStrategy.INCLUSIVE;
		}

        public DocBookSchemaResolutionStrategy getDocBookSchemaResolutionStrategy() {
            // TODO Maybe some are still using the legacy DTD stuff. In that case we want to make this overwritable
            return DocBookSchemaResolutionStrategy.XSD;
        }
    }

	private class ResourceDelegateImpl extends ResourceDelegateSupport {
		private ClassLoader loader;

		@Override
		protected ClassLoader getResourceClassLoader() {
			if ( loader == null ) {
				loader = buildResourceDelegateClassLoader();
			}
			return loader;
		}
	}

	@SuppressWarnings({ "unchecked" })
	private ClassLoader buildResourceDelegateClassLoader() {
		// There are three sources for resolver base urls:
		// 		1) staging dir
		//		2) project dependencies
		//		3) plugin dependencies (this should be plugin *injected* dependencies)
		List<URL> urls = new ArrayList<URL>();

		// 		1) staging dir
		if ( directoryLayout.getStagingDirectory().exists() ) {
			try {
				urls.add( directoryLayout.getStagingDirectory().toURI().toURL() );
			}
			catch ( MalformedURLException e ) {
				throw new JDocBookProcessException( "Unable to resolve staging directory to URL", e );
			}
		}

		//		2) project dependencies
		for ( Artifact artifact : (Set<Artifact>) project.getArtifacts() ) {
			if ( artifact.getFile() != null ) {
				try {
					urls.add( artifact.getFile().toURI().toURL() );
				}
				catch ( MalformedURLException e ) {
					getLog().warn( "Unable to retrieve artifact url [" + artifact.getId() + "]" );
				}
			}
		}

		//		3) plugin dependencies (this should be plugin *injected* dependencies)
		if ( pluginArtifacts != null ) {
			for ( Artifact artifact : (List<Artifact>) pluginArtifacts ) {
				if ( artifact.getFile() != null ) {
					try {
						urls.add( artifact.getFile().toURI().toURL() );
					}
					catch ( MalformedURLException e ) {
						getLog().warn( "Unable to retrieve artifact url [" + artifact.getId() + "]" );
					}
				}
			}
		}

		return new URLClassLoader(
				urls.toArray( new URL[ urls.size() ] ),
				Thread.currentThread().getContextClassLoader()
		);
	}

	private MasterLanguageDescriptorImpl masterLanguageDescriptor = new MasterLanguageDescriptorImpl();

	public MasterLanguageDescriptorImpl getMasterLanguageDescriptor() {
		return masterLanguageDescriptor;
	}

	private class MasterLanguageDescriptorImpl implements MasterLanguageDescriptor {
		public Locale getLanguage() {
			return fromLanguageString( masterTranslation );
		}

		public File getPotDirectory() {
			return directoryLayout.getPotSourceDirectory();
		}

		public File getBaseSourceDirectory() {
			return directoryLayout.getMasterSourceDirectory();
		}

		private File rootMasterFile;

		public File getRootDocumentFile() {
			if ( rootMasterFile == null ) {
				rootMasterFile = new File( getBaseSourceDirectory(), sourceDocumentName );
			}
			return rootMasterFile;
		}

		private Set<File> masterFiles;

		public Set<File> getDocumentFiles() {
			if ( masterFiles == null ) {
				File rootMasterFile = getRootDocumentFile();
				final Set<File> files = new TreeSet<File>();
				files.add( rootMasterFile );
				XIncludeHelper.findAllInclusionFiles( rootMasterFile, files );
				this.masterFiles = Collections.unmodifiableSet( files );
			}
			return masterFiles;
		}
	}

	private class ConfigurationImpl implements Configuration {
		private Options options() {
			return options;
		}

		public Map<String,String> getTransformerParameters() {
			return options().getTransformerParameters();
		}

		public boolean isUseRelativeImageUris() {
			return options().isUseRelativeImageUris();
		}

		public char getLocaleSeparator() {
			return options().getLocaleSeparator();
		}

		public boolean isAutoDetectFontsEnabled() {
			return options().isAutoDetectFontsEnabled();
		}

		public boolean isUseFopFontCacheEnabled() {
			return options().isUseFopFontCache();
		}

		private LinkedHashSet<ValueInjection> valueInjections;

		public LinkedHashSet<ValueInjection> getValueInjections() {
			if ( valueInjections == null ) {
				valueInjections = new LinkedHashSet<ValueInjection>();
                if ( injections != null ) {
				    valueInjections.addAll( Arrays.asList(injections) );
                }

				if ( options().isApplyStandardInjectionValues() ) {
					valueInjections.add( new ValueInjection( "version", project.getVersion() ) );
					SimpleDateFormat dateFormat = new SimpleDateFormat( options().getInjectionDateFormat() );
					valueInjections.add( new ValueInjection( "today", dateFormat.format( new Date() ) ) );
				}
			}
			return valueInjections;
		}

		private LinkedHashSet<String> catalogSet;

		public LinkedHashSet<String> getCatalogs() {
			if ( catalogSet == null ) {
				catalogSet = new LinkedHashSet<String>();
				for ( String catalog : options().getCatalogs() ) {
					catalogSet.add( catalog );
				}
			}
			return catalogSet;
		}

		public org.jboss.jdocbook.Profiling getProfiling() {
			return profiling;
		}

		public String getDocBookVersion() {
			return options.getDocbookVersion();
		}
	}

	public List<TranslationSource> getTranslationSources(boolean excludeIgnoredTranslations) {
		ArrayList<TranslationSource> result = new ArrayList<TranslationSource>();

		Locale requestedLocale = getRequestedLanguageLocale();
		boolean requestedLocaleIsTranslation = requestedLocale != null
				&& ! stringify( requestedLocale ).equals( masterTranslation );

		boolean skipAllTranslations = ignoreTranslations
				&& excludeIgnoredTranslations
				&& !requestedLocaleIsTranslation;

		if ( skipAllTranslations ) {
			getLog().info( "Skipping all translations" );
		}
		else {
			for ( String localeStr : translations ) {
				final Locale locale = parseLocale( localeStr );
				final boolean skipThisLocale = requestedLocale != null
						&& !requestedLocale.equals( locale )
						&& excludeIgnoredTranslations;
				if ( skipThisLocale ) {
					getLog().debug( "skipping non-requested lang [" + localeStr + "]" );
					continue;
				}
				result.add( new TranslationSourceImpl( localeStr ) );
			}
		}

		return result;
	}

	private class TranslationSourceImpl implements TranslationSource {
		private final String language;
		private final Locale languageLocale;

		private TranslationSourceImpl(String language) {
			this.language = language;
			this.languageLocale = fromLanguageString( language );
		}

		public Locale getLanguage() {
			return languageLocale;
		}

		public File resolvePoDirectory() {
			return directoryLayout.getTranslationSourceDirectory( language );
		}

		public File resolveTranslatedXmlDirectory() {
			return directoryLayout.getTranslationDirectory( language );
		}
	}

	public Locale fromLanguageString(String languageStr) {
		return TranslationUtils.parse( languageStr, options.getLocaleSeparator() );
	}
}
