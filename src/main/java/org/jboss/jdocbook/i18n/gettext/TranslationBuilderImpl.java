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
package org.jboss.jdocbook.i18n.gettext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.Log;
import org.jboss.jdocbook.i18n.I18nEnvironment;
import org.jboss.jdocbook.i18n.I18nSource;
import org.jboss.jdocbook.i18n.MasterTranslationDescriptor;
import org.jboss.jdocbook.i18n.TranslationBuilder;
import org.jboss.jdocbook.i18n.gettext.TranslatedItemFactory.TranslatedItem;
import org.jboss.jdocbook.util.FileUtils;
import org.jboss.jdocbook.util.XIncludeHelper;

/**
 * Implementation of the {@link org.jboss.jdocbook.i18n.TranslationBuilder} contract based on system calls
 * to the 'po2xml' command (part of the KDE poxml package).
 *
 * @author Steve Ebersole
 */
public class TranslationBuilderImpl implements TranslationBuilder {
	private final I18nEnvironment environment;

	public TranslationBuilderImpl(I18nEnvironment environment) {
		this.environment = environment;
	}

	private Log getLog() {
		return environment.log();
	}

	public void buildTranslations() throws JDocBookProcessException {
		MasterTranslationDescriptor master = environment.getMasterTranslationDescriptor();

		for ( I18nSource source : environment.getI18nSources( true ) ) {
			buildTranslation(
					master.resolveMasterDocument(),
					source.resolvePoDirectory(),
					source.resolveTranslatedXmlDirectory()
			);
		}
	}
	public void buildTranslation(File masterFile, File basePoDirectory,
			File targetDirectory) throws JDocBookProcessException {
		getLog().trace("starting translation [" + masterFile + "]");
		if (!masterFile.exists()) {
			getLog().info(
					"skipping translation; master file did not exist : {}",
					masterFile);
			return;
		}
		Set<File> files = new HashSet<File>();
		findAllInclusionFiles(masterFile, files);
		files.add(masterFile);
		Set<TranslatedItem> translatedItems = TranslatedItemFactory
				.createTranslatedItem(masterFile, basePoDirectory,
						targetDirectory, files);
		for (TranslatedItem item : translatedItems) {
			if (item.getSourceFile().getName().endsWith("xml")) {
				generateTranslatedXML(item.getSourceFile(), item.getPoFile(), item
						.getTargetFile());
			} else {
				copyNonXMLFileToWorkDir(item);
			}
		}
	}

	private void copyNonXMLFileToWorkDir(TranslatedItem item) {

		try {
			FileUtils.copyFileToDirectoryIfModified(item.getSourceFile(), item
					.getTargetFile().getParentFile());
		} catch (IOException e) {
			throw new JDocBookProcessException("unable to copy file [ "
					+ item.getSourceFile() + " ] to directory [ "
					+ item.getTargetFile().getParentFile() + " ]");
		}

	}

	/**
	 * Find all files referenced by master file, include indirectly inclusion.
	 * <p>
	 * {@link XIncludeHelper#locateInclusions(File)} may return files that do not exist or are not normal XML files.
	 * <p>
	 * For example:
	 * <p>
	 * 1. If a XML file has the following DOCTYPE, (this is asked by <tt>publican</tt>), then <em>Hibernate_Annotations_Reference_Guide.ent</em>
	 * will be returned by {@link XIncludeHelper#locateInclusions(File)}
	 * <blockquote><pre>
	 * &lt;!DOCTYPE chapter PUBLIC "-//OASIS//DTD DocBook XML V4.5//EN" "http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd" [ 
	 * &lt;!ENTITY % BOOK_ENTITIES SYSTEM "Hibernate_Annotations_Reference_Guide.ent"&gt;
	 * %BOOK_ENTITIES;
	 * ]&gt;
	 * </pre></blockquote>
	 * <p>
	 * 2. Publican can use the following style XInclude:
	 * <blockquote><pre>
	 * &lt;xi:include xmlns:xi="http://www.w3.org/2001/XInclude" href="Common_Content/Legal_Notice.xml"&gt;
	 * </pre></blockquote>
	 * This Legal_Notice.xml file actually do not exist in the current source directory, but in the predefined publican brand
	 * <p>
	 */
	private void findAllInclusionFiles(File masterFile, Set<File> files) {
		if(masterFile==null || !masterFile.exists() || !masterFile.getName().endsWith("xml")){
			return;
		}
		Set<File> inclusions = XIncludeHelper.locateInclusions(masterFile);
		if (inclusions == null || inclusions.isEmpty())
			return;
		for (File inclusion : inclusions) {
			if (!inclusion.exists()) {
				getLog().info(
						"skipping translation; inclusion file did not exist : [ "+
						inclusion+" ]");
				continue;
			}
			files.add(inclusion);
			findAllInclusionFiles(inclusion, files);
		}

	}

	private void generateTranslatedXML(File masterFile, File poFile, File translatedFile) {
		if ( !masterFile.exists() ) {
			getLog().trace( "skipping translation; source file did not exist : {0}", masterFile );
			return;
		}
		if ( !poFile.exists() ) {
			getLog().trace( "skipping translation; PO file did not exist : {0}", poFile );
			return;
		}

		if ( translatedFile.exists()
				&& translatedFile.lastModified() >= masterFile.lastModified()
				&& translatedFile.lastModified() >= poFile.lastModified() ) {
			getLog().trace( "skipping translation; up-to-date : {0}", translatedFile );
			return;
		}

		if ( ! translatedFile.getParentFile().exists() ) {
			boolean created = translatedFile.getParentFile().mkdirs();
			if ( ! created ) {
				getLog().info( "Unable to create directories for translation" );
			}
		}

		CommandLine commandLine = CommandLine.parse( "po2xml" );
		commandLine.addArgument( FileUtils.resolveFullPathName( masterFile ) );
		commandLine.addArgument( FileUtils.resolveFullPathName( poFile ) );

		try {
			final FileOutputStream xmlStream = new FileOutputStream( translatedFile );
			DefaultExecutor executor = new DefaultExecutor();
			try {
				PumpStreamHandler streamDirector = new PumpStreamHandler( xmlStream, System.err );
				executor.setStreamHandler( streamDirector );
				executor.execute( commandLine );
			}
			catch ( IOException ioe ) {
				throw new JDocBookProcessException( "unable to execute po2xml : " + ioe.getMessage() );
			}
			finally {
				try {
					xmlStream.flush();
					xmlStream.close();
				}
				catch ( IOException ignore ) {
					// intentionally empty...
				}
			}
		}
		catch ( IOException e  ) {
			throw new JDocBookProcessException( "unable to open output stream for translated XML file [" + translatedFile + "]" );
		}
	}
}
