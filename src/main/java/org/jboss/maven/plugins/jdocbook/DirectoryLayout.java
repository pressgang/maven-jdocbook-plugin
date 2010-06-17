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

import org.jboss.jdocbook.JDocBookProcessException;

/**
 * Represents the layout of the directories jDocBook will need.
 *
 * @author Steve Ebersole
 */
public class DirectoryLayout {
	public static interface BaseInfo {
		public String getMasterLanguage();
		public File getBaseSourceDirectory();
		public File getBaseOutputDirectory();
	}

	private final BaseInfo baseInfo;

	public DirectoryLayout(BaseInfo baseInfo) {
		this.baseInfo = baseInfo;
	}

	private boolean hasTranslations() {
		return ( ! ( baseInfo.getMasterLanguage() == null || "".equals( baseInfo.getMasterLanguage() ) ) );
	}

	private void ensureTranslations() {
		if ( !hasTranslations() ) {
			throw new JDocBookProcessException( "Project did not define language support" );
		}
	}


	// source dir layout ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public File getMasterSourceDirectory() {
		return hasTranslations()
				? getTranslationSourceDirectory( baseInfo.getMasterLanguage() )
				: baseInfo.getBaseSourceDirectory();
	}

	public File getPotSourceDirectory() {
		ensureTranslations();
		return getTranslationSourceDirectory( "pot" );
	}

	public File getTranslationSourceDirectory(String language) {
		ensureTranslations();
		return new File( baseInfo.getBaseSourceDirectory(), language );
	}


	// target directory ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private File targetDirectory;

	private File getTargetDirectory() {
		if ( targetDirectory == null ) {
			targetDirectory = new File( baseInfo.getBaseOutputDirectory(), "docbook" );
		}
		return targetDirectory;
	}


	// staging directory ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private File stagingDirectory;

	public File getStagingDirectory() {
		if ( stagingDirectory == null ) {
			stagingDirectory = new File( getTargetDirectory(), "staging" );
		}
		return stagingDirectory;
	}


 	// work directory ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private File rootJDocBookWorkDirectory;

	public File getRootJDocBookWorkDirectory() {
		if ( rootJDocBookWorkDirectory == null ) {
			rootJDocBookWorkDirectory = new File( getTargetDirectory(), "work" );
		}
		return rootJDocBookWorkDirectory;
	}


	// translation work dir layout ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private File rootJDocBookTranslationWorkDirectory;

	private File getRootJDocBookTranslationWorkDirectory() {
		if ( rootJDocBookTranslationWorkDirectory == null ) {
			rootJDocBookTranslationWorkDirectory = new File( getRootJDocBookWorkDirectory(), "translate" );
		}
		return rootJDocBookTranslationWorkDirectory;
	}

	public File getTranslationDirectory(String language) {
		return new File( getRootJDocBookTranslationWorkDirectory(), language );
	}


	// profile work dir layout ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private File rootJDocBookProfileWorkDirectory;

	private File getRootJDocBookProfileWorkDirectory() {
		if ( rootJDocBookProfileWorkDirectory == null ) {
			rootJDocBookProfileWorkDirectory = new File( getRootJDocBookWorkDirectory(), "profile" );
		}
		return rootJDocBookProfileWorkDirectory;
	}

	public File getProfilingDirectory(String language) {
		return new File( getRootJDocBookProfileWorkDirectory(), language );
	}


	// publish dir layout ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private File rootPublishDirectory;

	private File getRootPublishDirectory() {
		if ( rootPublishDirectory == null ) {
			rootPublishDirectory = new File( getTargetDirectory(), "publish" );
		}
		return rootPublishDirectory;
	}

	public File getPublishBaseDirectory(String language) {
		return new File( getRootPublishDirectory(), language );
	}

	public File getPublishDirectory(String language, String format) {
		return new File( getPublishBaseDirectory( language ), format );
	}


	// XSL-FO dir layout ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private File xslFoDirectory;

	public File getXslFoDirectory() {
		if ( xslFoDirectory == null ) {
			xslFoDirectory = new File( getRootJDocBookWorkDirectory(), "xsl-fo" );
		}
		return xslFoDirectory;
	}
}
