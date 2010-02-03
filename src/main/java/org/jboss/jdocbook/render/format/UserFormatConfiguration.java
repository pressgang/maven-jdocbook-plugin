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
package org.jboss.jdocbook.render.format;

/**
 * Models configuration choices of a user in regards to a particular rendering format.
 *
 * @author Steve Ebersole
 */
public class UserFormatConfiguration {
	protected String formatName;

	protected String targetFileExtension;
	protected String finalName;
	protected String stylesheetResource;
	protected Boolean imagePathSettingRequired;
	protected Boolean imageCopyingRequired;
	protected Boolean doingChunking;
	protected String profilingTypeName;

	/**
	 * Do not use!  Needed by Maven :(
	 */
	public UserFormatConfiguration() {
	}

	public UserFormatConfiguration(
			String formatName,
			String targetFileExtension,
			String finalName,
			String stylesheetResource,
			Boolean imagePathSettingRequired,
			Boolean imageCopyingRequired,
			Boolean doingChunking,
			String profilingTypeName) {
		this.formatName = formatName;
		this.targetFileExtension = targetFileExtension;
		this.finalName = finalName;
		this.stylesheetResource = stylesheetResource;
		this.imagePathSettingRequired = imagePathSettingRequired;
		this.imageCopyingRequired = imageCopyingRequired;
		this.doingChunking = doingChunking;
		this.profilingTypeName = profilingTypeName;
	}

	public String getFormatName() {
		return formatName;
	}

	public String getTargetFileExtension() {
		return targetFileExtension;
	}

	public String getFinalName() {
		return finalName;
	}

	public String getStylesheetResource() {
		return stylesheetResource;
	}

	public Boolean getImagePathSettingRequired() {
		return imagePathSettingRequired;
	}

	public Boolean getImageCopyingRequired() {
		return imageCopyingRequired;
	}

	public Boolean getDoingChunking() {
		return doingChunking;
	}

	public String getProfilingTypeName() {
		return profilingTypeName;
	}
}