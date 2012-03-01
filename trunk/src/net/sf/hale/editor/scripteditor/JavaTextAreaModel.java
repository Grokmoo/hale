/*
 * Copyright (c) 2008-2010, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sf.hale.editor.scripteditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Iterator;

import de.matthiasmann.twl.model.HasCallback;
import de.matthiasmann.twl.textarea.Style;
import de.matthiasmann.twl.textarea.StyleAttribute;
import de.matthiasmann.twl.textarea.StyleSheetKey;
import de.matthiasmann.twl.textarea.TextAreaModel;

/**
 * A text model for Java or similar code.  Parses supplied text and marks it up using
 * the standard conventions of a syntax highlighting text editor.
 * 
 * @author Matthias Mann
 */
public class JavaTextAreaModel extends HasCallback implements TextAreaModel {

    private final EnumMap<JavaScriptScanner.Kind, Style> styles;
    private final Style normalStyle;
    private ContainerElement root;

    /**
     * Creates a new, empty, JavaTextAreaModel.
     */
    
    public JavaTextAreaModel() {
        this.styles = new EnumMap<JavaScriptScanner.Kind, Style>(JavaScriptScanner.Kind.class);
        this.normalStyle = new Style(null, new StyleSheetKey("pre", "code", null))
                .with(StyleAttribute.PREFORMATTED, true);
        styles.put(JavaScriptScanner.Kind.NORMAL, normalStyle);
        styles.put(JavaScriptScanner.Kind.COMMENT, new Style(normalStyle, new StyleSheetKey("span", "comment", null)));
        styles.put(JavaScriptScanner.Kind.COMMENT_TAG, new Style(normalStyle, new StyleSheetKey("span", "commentTag", null)));
        styles.put(JavaScriptScanner.Kind.STRING, new Style(normalStyle, new StyleSheetKey("span", "string", null)));
        styles.put(JavaScriptScanner.Kind.KEYWORD, new Style(normalStyle, new StyleSheetKey("span", "keyword", null)));
    }

    @Override public Iterator<Element> iterator() {
        return new IteratorImpl(root);
    }

    /**
     * Clears all data from this JavaTextAreaModel.  It will be empty after this call completes.
     */
    
    public void clear() {
        root = null;
        doCallback();
    }

    /**
     * Add the text from the specified reader to this JavaTextModel.
     * 
     * @param r the reader to read text from
     * @param withLineNumbers true if line numbers should be shown prior to each line, false otherwise
     */
    
    public void parse(Reader r, boolean withLineNumbers) {
        JavaScriptScanner js = new JavaScriptScanner(r);

        ContainerElement container;
        Style lineStyle;
        if(withLineNumbers) {
            container = new OrderedListElement(new Style(normalStyle, new StyleSheetKey("ol", "linenumbers", null)), 1);
            lineStyle = new Style(container.getStyle(), new StyleSheetKey("li", null, null));
        } else {
            container = new BlockElement(normalStyle);
            lineStyle = null;
        }
        ContainerElement line = null;
        TextElement newLine = new TextElement(normalStyle, "\n");
        
        while(true) {
        	JavaScriptScanner.Kind kind = js.scan();
        	
        	if (kind == JavaScriptScanner.Kind.EOF) {
        		if (line != null) {
        			line.add(newLine);
                    container.add(line);
        		}
        		break;
        	}
        	
            if (withLineNumbers && line == null) {
                line = new ContainerElement(lineStyle);
            }
            
            if (kind == JavaScriptScanner.Kind.NEWLINE) {
            	if(line != null) {
            		line.add(newLine);
            		container.add(line);
            		line = null;
            	} else {
            		container.add(newLine);
            	}
            } else {
            	TextElement textElement = new TextElement(styles.get(kind), js.getString());
            	if(line != null) {
            		line.add(textElement);
            	} else {
            		container.add(textElement);
            	}
            }
        }

        root = container;
        doCallback();
    }

    /**
     * Adds the text from the specified InputStream to this JavaTextAreaModel.
     * 
     * @param is the InputStream to read text from
     * @param charsetName the nam of the CharacterSet used by the InputStream
     * @param withLineNumbers true if line numbers should be shown prior to each line, false otherwise
     * @throws UnsupportedEncodingException
     */
    
    public void parse(InputStream is, String charsetName, boolean withLineNumbers) throws UnsupportedEncodingException {
        InputStreamReader isr = new InputStreamReader(is, charsetName);
        parse(isr, withLineNumbers);
    }

    /**
     * Adds the text from the specified URL to this JavaTextAreaModel.
     * 
     * @param url the URL to read text from
     * @param withLineNumbers true if line numbers should be shown prior to each line, false otherwise
     * @throws IOException
     */
    
    public void parse(URL url, boolean withLineNumbers) throws IOException {
        InputStream is = url.openStream();
        try {
            parse(is, "UTF8", withLineNumbers);
        } finally {
            is.close();
        }
    }

    /**
     * Adds the text from the specified File to this JavaTextAreaModel.
     * 
     * @param file the File to read text from
     * @param withLineNumbers true if line numbers should be shown prior to each line, false otherwise
     * @throws IOException
     */
    
    public void parse(File file, boolean withLineNumbers) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            parse(fis, "UTF8", withLineNumbers);
        } finally {
            fis.close();
        }
    }

    private static class IteratorImpl implements Iterator<Element> {
        Element e;
        public IteratorImpl(Element e) {
            this.e = e;
        }

        @Override
		public boolean hasNext() {
            return e != null;
        }

        @Override
		public Element next() {
            Element tmp = e;
            e = null;
            return tmp;
        }

        @Override
		public void remove() {
        }
    }
}
