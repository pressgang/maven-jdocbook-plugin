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
import java.io.IOException;
import java.util.Locale;

import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.Log;
import org.jboss.jdocbook.i18n.PoSynchronizer;
import org.jboss.jdocbook.i18n.I18nEnvironment;
import org.jboss.jdocbook.i18n.I18nSource;
import org.jboss.jdocbook.util.FileUtils;
import org.jboss.jdocbook.util.I18nUtils;
import org.jboss.jdocbook.util.VCSDirectoryExclusionFilter;
import org.jboss.jdocbook.util.LocaleUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

/**
 * Implementation of the {@link PoSynchronizer} contract based on system calls
 * to either the 'msgmerge' or the 'msginit' commands (both part of the GNU
 * gettext package).
 *
 * @author Steve Ebersole
 */
public class PoSynchronizerImpl implements PoSynchronizer {
	private final I18nEnvironment environment;

	public PoSynchronizerImpl(I18nEnvironment environment) {
		this.environment = environment;
	}

	private Log getLog() {
		return environment.log();
	}

	public void synchronizePoFiles() {
		for ( I18nSource source : environment.getI18nSources( true ) ) {
			synchronizePo(
					environment.getMasterTranslationDescriptor().resolvePotDirectory(),
					source.resolvePoDirectory(),
					source.getLocale()
			);
		}
	}

	private void synchronizePo(File potDirectory, File poDirectory, Locale translationLocale)
			throws JDocBookProcessException {
		if ( !potDirectory.exists() ) {
			getLog().info( "skipping PO updates; POT directory did not exist : {0}", potDirectory );
			return;
		}
		File[] files = potDirectory.listFiles( new VCSDirectoryExclusionFilter() );
		for ( int i = 0, X = files.length; i < X; i++) {
			if ( files[i].isDirectory() ) {
				// recurse into the directory by calling back into ourselves with the sub-dir
				synchronizePo(
						new File( potDirectory, files[i].getName() ),
						new File( poDirectory, files[i].getName() ),
						translationLocale
				);
			}
			else {
				if ( I18nUtils.isPotFile( files[i] ) ) {
					File translation = new File( poDirectory, I18nUtils.determinePoFileName( files[i] ) );
					updateTranslation( files[i], translation, translationLocale );
				}
			}
		}
	}

	private void updateTranslation(File template, File translation, Locale translationLocale) {
		if ( !template.exists() ) {
			getLog().trace( "skipping PO updates; POT file did not exist : {0}", template );
			return;
		}

		if ( translation.lastModified() >= template.lastModified() ) {
			getLog().trace( "skipping PO updates; up-to-date : {0}", translation );
			return;
		}

		final String translationLocaleString = LocaleUtils.render( translationLocale, environment.getOptions().getLocaleSeparator() );

		CommandLine commandLine;
		if ( translation.exists() ) {
			commandLine = CommandLine.parse( "msgmerge" );
			commandLine.addArgument( "--quiet" );
			commandLine.addArgument( "--update" );
			commandLine.addArgument( "--backup=none" );
			commandLine.addArgument( FileUtils.resolveFullPathName( translation ) );
			commandLine.addArgument( FileUtils.resolveFullPathName( template ) );
		}
		else {
			if ( ! translation.getParentFile().exists() ) {
				boolean created = translation.getParentFile().mkdirs();
				if ( ! created ) {
					getLog().info( "Unable to create PO directory {}", translation.getParentFile().getAbsolutePath() );
				}
			}
			commandLine = CommandLine.parse( "msginit" );
			commandLine.addArgument( "--no-translator" );
			commandLine.addArgument( "--locale=" + translationLocaleString );
			commandLine.addArgument( "-i" );
			commandLine.addArgument( FileUtils.resolveFullPathName( template ) );
			commandLine.addArgument( "-o" );
			commandLine.addArgument( FileUtils.resolveFullPathName( translation ) );
		}

		getLog().info( "po-synch -> " + commandLine.toString() );

		DefaultExecutor executor = new DefaultExecutor();
		try {
			executor.execute( commandLine );
		}
		catch ( IOException e ) {
			throw new JDocBookProcessException( "Error synchronizing PO file [" + template.getName() + "] for " + translationLocaleString, e );
		}
	}
}
