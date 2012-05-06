/*
 * Copyright 2006 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ess.regexutil.gwt.regexEditor.client.parser;

import com.ess.regexutil.gwt.psi.client.lexer.IElementType;
import com.ess.regexutil.gwt.psi.client.lexer.TokenSet;

public interface RegExpElementTypes {
    IElementType PATTERN = new IElementType("PATTERN");
    IElementType BRANCH = new IElementType("BRANCH");
    IElementType CLOSURE = new IElementType("COUNTED_CLOSURE");
    IElementType QUANTIFIER = new IElementType("QUANTIFIER");
    IElementType SIMPLE_CLASS = new IElementType("SIMPLE_CLASS");
    IElementType CLASS = new IElementType("CLASS");
    IElementType CHAR_RANGE = new IElementType("CHAR_RANGE");
    IElementType INTERSECTION = new IElementType("INTERSECTION");
    IElementType CHAR = new IElementType("CHAR");
    IElementType GROUP = new IElementType("GROUP");
    IElementType PROPERTY = new IElementType("PROPERTY");
    IElementType OPTIONS = new IElementType("OPTIONS");
    IElementType SET_OPTIONS = new IElementType("SET_OPTIONS");
    IElementType BACKREF = new IElementType("BACKREF");
    IElementType BOUNDARY = new IElementType("BOUNDARY");
    IElementType PY_NAMED_GROUP_REF = new IElementType("PY_NAMED_GROUP_REF");
    IElementType PY_COND_REF = new IElementType("PY_COND_REF");

    TokenSet ATOMS = TokenSet.create(CLOSURE, BOUNDARY,
            SIMPLE_CLASS, CLASS, CHAR, GROUP, PROPERTY, SET_OPTIONS, BACKREF, PY_NAMED_GROUP_REF);

    TokenSet CLASS_ELEMENTS = TokenSet.create(CHAR, CHAR_RANGE,
                                              SIMPLE_CLASS, CLASS, INTERSECTION, PROPERTY);
}
