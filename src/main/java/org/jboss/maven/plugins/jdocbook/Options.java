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

import java.util.HashMap;
import java.util.Map;

/**
 * Maven non-sense.  Simply pulls Options into the namespace in regards to plugin config.
 *
 * @author Steve Ebersole
 */
public class Options {
	public static final String DEFAULT_STANDARD_DATE_INJECTION_FORMAT = "yyyy-MM-dd";
	private boolean xincludeSupported;
	public boolean isXincludeSupported() {
		return xincludeSupported;
	}
	public void setXincludeSupported( boolean xincludeSupported ) {
		this.xincludeSupported = xincludeSupported;
	}
	//TODO not used, maybe we should deprecate it, I add it here only for keeping the compatibility.
	private String xmlTransformerType;
	public String getXmlTransformerType() {
		return xmlTransformerType;
	}

	public void setXmlTransformerType( String xmlTransformerType ) {
		this.xmlTransformerType = xmlTransformerType;
	}
	private String docbookVersion;
	
	public String getDocbookVersion() {
		return docbookVersion;
	}

	public void setDocbookVersion( String docbookVersion ) {
		this.docbookVersion = docbookVersion;
	}
	private String[] catalogs = new String[0];

	public String[] getCatalogs() {
		return catalogs;
	}

	private Map<String,String> transformerParameters;

	public Map<String,String> getTransformerParameters() {
		if ( transformerParameters == null ) {
			transformerParameters = new HashMap<String, String>();
		}
		return transformerParameters;
	}

	private boolean useRelativeImageUris = true;

	public boolean isUseRelativeImageUris() {
		return useRelativeImageUris;
	}

	private char localeSeparator = '-';

	public char getLocaleSeparator() {
		return localeSeparator;
	}

	private boolean autoDetectFonts;

	public boolean isAutoDetectFontsEnabled() {
		return autoDetectFonts;
	}

	private boolean useFopFontCache = true;

	public boolean isUseFopFontCache() {
		return useFopFontCache;
	}

	private boolean applyStandardInjectionValues = true;

	public boolean isApplyStandardInjectionValues() {
		return applyStandardInjectionValues;
	}

	private String injectionDateFormat;

	public String getInjectionDateFormat() {
		return injectionDateFormat == null
				? DEFAULT_STANDARD_DATE_INJECTION_FORMAT
				: injectionDateFormat;
	}
}
