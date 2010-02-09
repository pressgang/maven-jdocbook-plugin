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

import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.Log;
import org.jboss.jdocbook.i18n.PotSynchronizer;
import org.jboss.jdocbook.i18n.I18nEnvironment;
import org.jboss.jdocbook.util.FileUtils;
import org.jboss.jdocbook.util.I18nUtils;
import org.jboss.jdocbook.util.XIncludeHelper;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * Implementation of the {@link PotSynchronizer} contract based on system calls
 * to the xml2pot command (part of the KDE poxml package).
 *
 * @author Steve Ebersole
 */
public class PotSynchronizerImpl implements PotSynchronizer {
	private I18nEnvironment environment;

	public PotSynchronizerImpl(I18nEnvironment environment) {
		this.environment = environment;
	}

	private Log getLog() {
		return environment.log();
	}

	/**
	 * {@inheritDoc} 
	 */
	public void synchronizePot() throws JDocBookProcessException {
		synchronizePot(
				environment.getMasterTranslationDescriptor().resolveMasterDocument(),
				environment.getMasterTranslationDescriptor().resolvePotDirectory()
		);
	}

	private void synchronizePot(File masterFile, File potDirectory)
			throws JDocBookProcessException {
		Set<File> files = new HashSet<File>();
		XIncludeHelper.findAllInclusionFiles(masterFile, files);
		files.add(masterFile);
		File baseDir = masterFile.getParentFile();
		for (File file : files) {
			String relativity = FileUtils.determineRelativity(file, baseDir);
			File relativeTranslationDir = (relativity == null) ? potDirectory
					: new File(potDirectory, relativity);
			if (FileUtils.isXMLFile(file)) {
				String poFileName = I18nUtils.determinePotFileName(file);
				File potFile = new File(relativeTranslationDir, poFileName);
				updatePortableObjectTemplate(file, potFile);
			}
		}
	}

	private void updatePortableObjectTemplate(File masterFile, File potFile) {
		if ( !masterFile.exists() ) {
			getLog().trace( "skipping POT update; source file did not exist : {0}", masterFile );
			return;
		}

		if ( potFile.exists() && potFile.lastModified() >= masterFile.lastModified() ) {
			getLog().trace( "skipping POT update; up-to-date : {0}", potFile );
			return;
		}

		if ( !potFile.getParentFile().exists() ) {
			boolean created = potFile.getParentFile().mkdirs();
			if ( !created ) {
				getLog().info( "Unable to generate POT directory {}" + FileUtils.resolveFullPathName( potFile.getParentFile() ) );
			}
		}
		executeXml2pot( masterFile, potFile );
	}

	private void executeXml2pot(File masterFile, File potFile) {
		CommandLine commandLine = CommandLine.parse( "xml2pot" );
		commandLine.addArgument( FileUtils.resolveFullPathName( masterFile ) );

		DefaultExecutor executor = new DefaultExecutor();

		try {
			final FileOutputStream xmlStream = new FileOutputStream( potFile );
			PumpStreamHandler streamDirector = new PumpStreamHandler( xmlStream, System.err );
			executor.setStreamHandler( streamDirector );
			try {
				getLog().trace( "updating POT file {0}", potFile );
				executor.execute( commandLine );
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
			throw new JDocBookProcessException( "unable to open output stream for POT file [" + potFile + "]" );
		}
	}
}
