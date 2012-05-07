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
package com.ess.regexutil.gwt.regexEditor.client.lexer;

import com.ess.regexutil.gwt.psi.client.TextStyle;
import com.ess.regexutil.gwt.psi.client.lexer.IElementType;
import com.ess.regexutil.gwt.psi.client.lexer.TokenSet;

public interface RegExpTT {
    IElementType NUMBER = new IElementType("NUMBER", TextStyle.foregroundStyle(0x0000FF));
    IElementType NAME = new IElementType("NAME");
    IElementType COMMA = new IElementType("COMMA");

    // "\\Q"
    IElementType QUOTE_BEGIN = new IElementType("QUOTE_BEGIN");
    // <QUOTE> "\\E"
    IElementType QUOTE_END = new IElementType("QUOTE_END");

    // "\\" 0-9
    IElementType BACKREF = new IElementType("BACKREF");

    IElementType LBRACE = new IElementType("LBRACE");
    IElementType RBRACE = new IElementType("RBRACE");

    IElementType CLASS_BEGIN = new IElementType("CLASS_BEGIN");
    IElementType CLASS_END = new IElementType("CLASS_END");
    IElementType ANDAND = new IElementType("ANDAND");

    IElementType GROUP_BEGIN = new IElementType("GROUP_BEGIN");
    IElementType GROUP_END = new IElementType("GROUP_END");

    IElementType NON_CAPT_GROUP = new IElementType("NON_CAPT_GROUP");
    IElementType POS_LOOKBEHIND = new IElementType("POS_LOOKBEHIND");
    IElementType NEG_LOOKBEHIND = new IElementType("NEG_LOOKBEHIND");
    IElementType POS_LOOKAHEAD = new IElementType("POS_LOOKAHEAD");
    IElementType NEG_LOOKAHEAD = new IElementType("NEG_LOOKAHEAD");
    IElementType SET_OPTIONS = new IElementType("SET_OPTIONS");

    IElementType QUEST = new IElementType("QUEST");
    IElementType STAR = new IElementType("STAR");
    IElementType PLUS = new IElementType("PLUS");
    IElementType COLON = new IElementType("COLON");

    // "\\" ("b" | "B" | "A" | "z" | "Z" | "G")
    IElementType BOUNDARY = new IElementType("BOUNDARY");
    // "^"
    IElementType CARET = new IElementType("CARET");
    // "$"
    IElementType DOLLAR = new IElementType("DOLLAR");

    IElementType DOT = new IElementType("DOT");
    IElementType UNION = new IElementType("UNION");

    // > in Python/Ruby named group
    IElementType GT = new IElementType("GT");
    // ' in Ruby quoted named group
    IElementType QUOTE = new IElementType("QUOTE");

    // "\b" | "\t" | "\f" | "\r" | "\n"
    IElementType CTRL_CHARACTER = new IElementType("CTRL_CHARACTER");
    // "\\" ("t" | "n" | "r" | "f" | "a" | "e")
    IElementType ESC_CTRL_CHARACTER = new IElementType("ESC_CTRL_CHARACTER");
    // "\\" ("." | "|" | "$" | "^" | "?" | "*" | "+" | "[" | "{" | "(" | ")")
    IElementType ESC_CHARACTER = new IElementType("ESC_CHARACTER");
    // "\\" ("w" | "W" | "s" | "S" | "d" | "D")
    IElementType CHAR_CLASS = new IElementType("CHAR_CLASS");
    // "\\u" XXXX
    IElementType UNICODE_CHAR = new IElementType("UNICODE_CHAR");
    // "\\x" XX
    IElementType HEX_CHAR = new IElementType("HEX_CHAR");
    // "\\0" OOO
    IElementType OCT_CHAR = new IElementType("OCT_CHAR");
    // "\\c" x
    IElementType CTRL = new IElementType("CTRL");
    // "\\p" | "\\P"
    IElementType PROPERTY = new IElementType("PROPERTY");

    // e.g. "\\#" but also "\\q" which is not a valid escape actually
    IElementType REDUNDANT_ESCAPE = new IElementType("REDUNDANT_ESCAPE");

    IElementType MINUS = new IElementType("MINUS");
    IElementType CHARACTER = new IElementType("CHARACTER");

    IElementType BAD_CHARACTER = new IElementType("BAD_CHARACTER");
    IElementType BAD_OCT_VALUE = new IElementType("BAD_OCT_VALUE");
    IElementType BAD_HEX_VALUE = new IElementType("BAD_HEX_VALUE");

    IElementType COMMENT = new IElementType("COMMENT");
    IElementType OPTIONS_ON = new IElementType("OPTIONS_ON");
    IElementType OPTIONS_OFF = new IElementType("OPTIONS_OFF");

    // (?P<name>...
    IElementType PYTHON_NAMED_GROUP = new IElementType("PYTHON_NAMED_GROUP");
    // (?P=name)
    IElementType PYTHON_NAMED_GROUP_REF = new IElementType("PYTHON_NAMED_GROUP_REF");
    // (?(id/name)yes-pattern|no-pattern)
    IElementType PYTHON_COND_REF = new IElementType("PYTHON_COND_REF");
  
    // (?<name>...
    IElementType RUBY_NAMED_GROUP = new IElementType("RUBY_NAMED_GROUP");

    // (?'name'...
    IElementType RUBY_QUOTED_NAMED_GROUP = new IElementType("RUBY_QUOTED_NAMED_GROUP");

    IElementType INVALID_CHARACTER_ESCAPE_TOKEN = new IElementType("INVALID_CHARACTER_ESCAPE_TOKEN");
    IElementType VALID_STRING_ESCAPE_TOKEN = new IElementType("VALID_STRING_ESCAPE_TOKEN");
    IElementType INVALID_UNICODE_ESCAPE_TOKEN = new IElementType("INVALID_UNICODE_ESCAPE_TOKEN");

    TokenSet KEYWORDS = TokenSet.create(DOT, STAR, QUEST, PLUS);

    TokenSet CHARACTERS = TokenSet.create(CHARACTER,
            ESC_CTRL_CHARACTER,
            ESC_CHARACTER,
            CTRL_CHARACTER,
            UNICODE_CHAR,
            HEX_CHAR, BAD_HEX_VALUE,
            OCT_CHAR, BAD_OCT_VALUE,
            REDUNDANT_ESCAPE,
            MINUS,
            INVALID_UNICODE_ESCAPE_TOKEN,
            INVALID_CHARACTER_ESCAPE_TOKEN
    );

    TokenSet SIMPLE_CLASSES = TokenSet.create(DOT, CHAR_CLASS);

    // caret is just a character in classes after the first position: [a^] matches "a" or "^"
    TokenSet CHARACTERS2 = TokenSet.orSet(CHARACTERS, SIMPLE_CLASSES, TokenSet.create(CARET, LBRACE));

    TokenSet QUANTIFIERS = TokenSet.create(QUEST, PLUS, STAR, LBRACE);

    TokenSet GROUPS = TokenSet.create(GROUP_BEGIN,
            NON_CAPT_GROUP,
            POS_LOOKAHEAD,
            NEG_LOOKAHEAD,
            POS_LOOKBEHIND,
            NEG_LOOKBEHIND);

    TokenSet BOUNDARIES = TokenSet.create(BOUNDARY, CARET, DOLLAR);
}
