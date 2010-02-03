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
package org.jboss.jdocbook.util;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.TransformerException;

import org.xml.sax.Attributes;

/**
 * Enumeration of supported XSLT transformers.
 *
 * @author Steve Ebersole
 */
public abstract class TransformerType {
	public static final TransformerType SAXON = new SaxonTransformerType();
	public static final TransformerType XALAN = new XalanTransformerType();

	private final String name;

	private SAXTransformerFactory factory;

	private TransformerType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public SAXTransformerFactory getSAXTransformerFactory() {
		if ( factory == null ) {
			factory = buildSAXTransformerFactory();
		}
		return factory;
	}

	protected abstract SAXTransformerFactory buildSAXTransformerFactory();

	public static TransformerType parse(String name) {
		if ( XALAN.name.equals( name ) ) {
			return XALAN;
		}
		else {
			// default
			return SAXON;
		}
	}

	/**
	 * The SAXON transformer type...
	 */
	public static class SaxonTransformerType extends TransformerType {
		public SaxonTransformerType() {
			super( "saxon" );
		}

		public SAXTransformerFactory buildSAXTransformerFactory() {
			com.icl.saxon.TransformerFactoryImpl factoryImpl = new com.icl.saxon.TransformerFactoryImpl();
			factoryImpl.setAttribute( "http://icl.com/saxon/feature/messageEmitterClass", SaxonXslMessageEmitter.class.getName() );
			return factoryImpl;
		}
	}

	public static class SaxonXslMessageEmitter extends com.icl.saxon.output.Emitter {
		private StringBuffer buffer;
		public void startDocument() throws TransformerException {
			if ( buffer != null ) {
				System.out.println( "Unexpected call sequence on SaxonXslMessageEmitter; discarding [" + buffer.toString() + "]" );
			}
			buffer = new StringBuffer();
		}

		public void endDocument() throws TransformerException {
			System.out.println( "[STYLESHEET MESSAGE] " + buffer.toString() );
			buffer.setLength( 0 );
			buffer = null;
		}

		public void startElement(int i, Attributes attributes, int[] ints, int i1) throws TransformerException {
		}

		public void endElement(int i) throws TransformerException {
		}

		public void characters(char[] chars, int start, int end) throws TransformerException {
			for ( int i = start; i < end; i++ ) {
				buffer.append( chars[i] );
			}
		}

		public void processingInstruction(String s, String s1) throws TransformerException {
		}

		public void comment(char[] chars, int i, int i1) throws TransformerException {
		}
	}

	public static class XalanTransformerType extends TransformerType {
		public XalanTransformerType() {
			super( "xalan" );
		}

		public SAXTransformerFactory buildSAXTransformerFactory() {
			return new org.apache.xalan.processor.TransformerFactoryImpl();
		}
	}
}