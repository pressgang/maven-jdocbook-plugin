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
package org.jboss.jdocbook;

import java.util.Properties;

import org.jboss.jdocbook.util.TransformerType;

/**
 * A (detachable) representation of the user configuration.
 *
 * @author Steve Ebersole
 */
public class Options {
	public static final String DEFAULT_STANDARD_DATE_INJECTION_FORMAT = "MMMM d, yyyy";

    private boolean xincludeSupported;
	private String[] catalogs;
	private String xmlTransformerType;
	private Properties transformerParameters;
	private boolean useRelativeImageUris = true;
	private String docbookVersion;
	private char localeSeparator = '-';
	private boolean autoDetectFonts;
	private boolean useFopFontCache = true;
	private boolean applyStandardInjectionValues = true;
	private String injectionDateFormat;

	protected Options() {
	}

	public Options(char localeSeparator) {
		this.localeSeparator = localeSeparator;
	}

	public Options(
			boolean xincludeSupported,
			String[] catalogs,
			String xmlTransformerType,
			Properties transformerParameters,
			boolean useRelativeImageUris,
			String docBookVersion,
			char localeSeparator,
			boolean autoDetectFonts) {
		this.xincludeSupported = xincludeSupported;
		this.catalogs = catalogs;
		this.xmlTransformerType = xmlTransformerType;
		this.transformerParameters = transformerParameters;
		this.useRelativeImageUris = useRelativeImageUris;
		this.docbookVersion = docBookVersion;
		this.localeSeparator = localeSeparator;
		this.autoDetectFonts = autoDetectFonts;
	}

	public boolean isXincludeSupported() {
		return xincludeSupported;
	}

	public String[] getCatalogs() {
		return catalogs;
	}

	public String getXmlTransformerType() {
		return xmlTransformerType;
	}

	public TransformerType resolveXmlTransformerType() {
		return TransformerType.parse( getXmlTransformerType() );
	}

	public Properties getTransformerParameters() {
		if ( transformerParameters == null ) {
			transformerParameters = new Properties();
		}
		return transformerParameters;
	}

	public boolean isUseRelativeImageUris() {
		return useRelativeImageUris;
	}

	public String getDocbookVersion() {
		return docbookVersion;
	}

	public void setDocbookVersion(String docbookVersion) {
		this.docbookVersion = docbookVersion;
	}

	public char getLocaleSeparator() {
		return localeSeparator;
	}

	public boolean isAutoDetectFontsEnabled() {
		return autoDetectFonts;
	}

	public void setAutoDetectFonts(boolean autoDetectFonts) {
		this.autoDetectFonts = autoDetectFonts;
	}

	public boolean isUseFopFontCache() {
		return useFopFontCache;
	}

	public void setUseFopFontCache(boolean useFopFontCache) {
		this.useFopFontCache = useFopFontCache;
	}

	public boolean isApplyStandardInjectionValues() {
		return applyStandardInjectionValues;
	}

	public void setApplyStandardInjectionValues(boolean applyStandardInjectionValues) {
		this.applyStandardInjectionValues = applyStandardInjectionValues;
	}

	public String getInjectionDateFormat() {
		return injectionDateFormat == null
				? DEFAULT_STANDARD_DATE_INJECTION_FORMAT
				: injectionDateFormat;
	}

	public void setInjectionDateFormat(String injectionDateFormat) {
		this.injectionDateFormat = injectionDateFormat;
	}
}