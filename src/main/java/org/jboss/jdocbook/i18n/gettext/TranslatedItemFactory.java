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
import java.util.HashSet;
import java.util.Set;

import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.util.FileUtils;
import org.jboss.jdocbook.util.I18nUtils;

/**
 * 
 * @author Strong Liu <stliu@redhat.com>
 */
public class TranslatedItemFactory {
	
	/**
	 * 
	 * @param masterFile 
	 * @param basePoDirectory
	 * @param baseTargetDirectory 
	 * @param inclusionFileSet The set of files referenced by masterFile via XIncludes.
	 * @return a set of <code>TranslatedItem</code> items.
	 */
	public static Set<TranslatedItem> createTranslatedItem(File masterFile,
			File basePoDirectory, File baseTargetDirectory,
			Set<File> inclusionFileSet) {
		Set<TranslatedItem> translatedItemSet = new HashSet<TranslatedItem>();
		File baseDir = masterFile.getParentFile();
		translatedItemSet.add(createTranslatedItem(masterFile, baseDir,
				basePoDirectory, baseTargetDirectory));
		for (File file : inclusionFileSet) {
			translatedItemSet.add(createTranslatedItem(file, baseDir,
					basePoDirectory, baseTargetDirectory));
		}
		return translatedItemSet;
	}

	private static TranslatedItem createTranslatedItem(File file, File baseDir,
			File basePoDirectory, File baseTargetDirectory) {
		String relativity = FileUtils.determineRelativity(file, baseDir);
		File relativeTranslationDir = (relativity == null) ? basePoDirectory
				: new File(basePoDirectory, relativity);
		File relativeWorkDir = (relativity == null) ? baseTargetDirectory
				: new File(baseTargetDirectory, relativity);
		File poFile = null;
		if (isXMLFile(file)) {
			String poFileName = I18nUtils.determinePoFileName(file);

			poFile = new File(relativeTranslationDir, poFileName);
			if (!poFile.exists()) {
				throw new JDocBookProcessException(
						"Unable to locate PO file for [" + file + "] in ["
								+ basePoDirectory + "]");
			}
		}
		File translatedFile = new File(relativeWorkDir, file.getName());
		return new TranslatedItem(file, poFile, translatedFile);
	}
	
	private static boolean isXMLFile(File file){
		return file!=null && file.exists() && file.getName().endsWith("xml");
	}
	
	public static class TranslatedItem {
		private File sourceFile;
		private File poFile;
		private File targetFile;

		private TranslatedItem(File sourceFile, File poFile, File targetFile) {
			this.sourceFile = sourceFile;
			this.poFile = poFile;
			this.targetFile = targetFile;
		}

		public File getSourceFile() {
			return sourceFile;
		}

		public File getPoFile() {
			return poFile;
		}

		public File getTargetFile() {
			return targetFile;
		}
	}
}
