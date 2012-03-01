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

import java.io.Reader;

/**
 * A simple JavaScript source file parser for syntax highlighting.
 *
 * @author Matthias Mann
 * @author Jared Stephen
 */
public class JavaScriptScanner {

	/**
	 * All the different types of tokens for JavaScript code that are recognized
	 * @author Matthias Mann
	 *
	 */
    public enum Kind {
        /** End of file - this token has no text */
        EOF,
        /** End of line - this token has no text */
        NEWLINE,
        /** Normal text - does not include line breaks */
        NORMAL,
        /** A keyword */
        KEYWORD,
        /** A string or character constant */
        STRING,
        /** A comment - multi line comments are split up and and NEWLINE tokens are inserted */
        COMMENT,
        /** A javadoc tag inside a comment */
        COMMENT_TAG
    }

    private static final KeywordList KEYWORD_LIST = new KeywordList(
            "abstract", "as", "assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue", "debugger", "default", "delete", "do", "double",
            "else", "enum", "extends", "false", "final", "finally", "float",
            "for", "function", "goto", "if", "implements", "import", "in", "instanceof", "int",
            "interface", "long", "namespace", "native", "new", "null", "package", "private",
            "protected", "public", "return", "short", "static",
            "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "true", "try", "typeof", "use", "var", "void", "volatile", "while", "with");

    private final CharacterIterator iterator;

    private boolean inMultiLineComment;
    
    /**
     * Creates a new JavaScriptScanner that will scan the specified Reader
     * @param r the reader to be scanned
     */
    
    public JavaScriptScanner(Reader r) {
        this.iterator = new CharacterIterator(r);
    }

    /**
     * Scans for the next token.
     * Read errors result in EOF.
     *
     * Use {@link #getString()} to retrieve the string for the parsed token.
     * 
     * @return the next token.
     */
    public Kind scan() {
        iterator.clear();
        if(inMultiLineComment) {
            return scanMultiLineComment(false);
        }
        int ch = iterator.next();
        switch(ch) {
            case CharacterIterator.EOF:
                return Kind.EOF;
            case '\n':
                return Kind.NEWLINE;
            case '\"':
            case '\'':
                scanString(ch);
                return Kind.STRING;
            case '/':
                switch(iterator.peek()) {
                    case '/':
                        iterator.advanceToEOL();
                        return Kind.COMMENT;
                    case '*':
                        inMultiLineComment = true;
                        iterator.next(); // skip '*'
                        return scanMultiLineComment(true);
                }
                // fall through
            default:
                return scanNormal(ch);
        }
    }

    /**
     * Returns the string for the last token returned by {@link #scan() }
     *
     * @return the string for the last token
     */
    public String getString() {
    	// hack to replace tabs with 4 spaces so code is formatted in a nicer way
    	return iterator.getString().replace("\t", "    ");
    }

    private void scanString(int endMarker) {
        for(;;) {
            int ch = iterator.next();
            if(ch == '\\') {
                iterator.next();
            } else if(ch == endMarker || ch == '\n') {
                return;
            }
        }
    }
    
    private Kind scanMultiLineComment(boolean start) {
        int ch = iterator.next();
        if(!start && ch == '\n') {
            return Kind.NEWLINE;
        }
        if(ch == '@') {
            iterator.advanceIdentifier();
            return Kind.COMMENT_TAG;
        }
        for(;;) {
            if(ch < 0 || (ch == '*' && iterator.peek() == '/')) {
                iterator.next();
                inMultiLineComment = false;
                return Kind.COMMENT;
            }
            if(ch == '\n') {
                iterator.pushback();
                return Kind.COMMENT;
            }
            if(ch == '@') {
                iterator.pushback();
                return Kind.COMMENT;
            }
            ch = iterator.next();
        }
    }

    private Kind scanNormal(int ch) {
        for(;;) {
            switch(ch) {
                case '\n':
                case '\"':
                case '\'':
                case CharacterIterator.EOF:
                    iterator.pushback();
                    return Kind.NORMAL;
                case '/':
                    if(iterator.check("/*")) {
                        iterator.pushback();
                        return Kind.NORMAL;
                    }
                    break;
                default:
                    if(Character.isJavaIdentifierStart(ch)) {
                        iterator.setMarker(true);
                        iterator.advanceIdentifier();
                        if(iterator.isKeyword(KEYWORD_LIST)) {
                            if(iterator.isMarkerAtStart()) {
                                return Kind.KEYWORD;
                            }
                            iterator.rewindToMarker();
                            return Kind.NORMAL;
                        }
                    }
                    break;
            }
            ch = iterator.next();
        }
    }
    
}
