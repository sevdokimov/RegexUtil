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

import com.ess.regexutil.gwt.psi.client.lexer.TokenType;
import com.ess.regexutil.gwt.psi.client.lexer.TokenSet;

public interface RegExpTT {
    int NUMBER = TokenType.createToken("NUMBER");
    int NAME = TokenType.createToken("NAME");
    int COMMA = TokenType.createToken("COMMA");

    // "\\Q"
    int QUOTE_BEGIN = TokenType.createToken("QUOTE_BEGIN");
    // <QUOTE> "\\E"
    int QUOTE_END = TokenType.createToken("QUOTE_END");

    // "\\" 0-9
    int BACKREF = TokenType.createToken("BACKREF");

    int LBRACE = TokenType.createToken("LBRACE");
    int RBRACE = TokenType.createToken("RBRACE");

    int CLASS_BEGIN = TokenType.createToken("CLASS_BEGIN");
    int CLASS_END = TokenType.createToken("CLASS_END");
    int ANDAND = TokenType.createToken("ANDAND");

    int GROUP_BEGIN = TokenType.createToken("GROUP_BEGIN");
    int GROUP_END = TokenType.createToken("GROUP_END");

    int NON_CAPT_GROUP = TokenType.createToken("NON_CAPT_GROUP");
    int POS_LOOKBEHIND = TokenType.createToken("POS_LOOKBEHIND");
    int NEG_LOOKBEHIND = TokenType.createToken("NEG_LOOKBEHIND");
    int POS_LOOKAHEAD = TokenType.createToken("POS_LOOKAHEAD");
    int NEG_LOOKAHEAD = TokenType.createToken("NEG_LOOKAHEAD");
    int SET_OPTIONS = TokenType.createToken("SET_OPTIONS");

    int QUEST = TokenType.createToken("QUEST");
    int STAR = TokenType.createToken("STAR");
    int PLUS = TokenType.createToken("PLUS");
    int COLON = TokenType.createToken("COLON");

    // "\\" ("b" | "B" | "A" | "z" | "Z" | "G")
    int BOUNDARY = TokenType.createToken("BOUNDARY");
    // "^"
    int CARET = TokenType.createToken("CARET");
    // "$"
    int DOLLAR = TokenType.createToken("DOLLAR");

    int DOT = TokenType.createToken("DOT");
    int UNION = TokenType.createToken("UNION");

    // > in Python/Ruby named group
    int GT = TokenType.createToken("GT");
    // ' in Ruby quoted named group
    int QUOTE = TokenType.createToken("QUOTE");

    // "\b" | "\t" | "\f" | "\r" | "\n"
    int CTRL_CHARACTER = TokenType.createToken("CTRL_CHARACTER");
    // "\\" ("t" | "n" | "r" | "f" | "a" | "e")
    int ESC_CTRL_CHARACTER = TokenType.createToken("ESC_CTRL_CHARACTER");
    // "\\" ("." | "|" | "$" | "^" | "?" | "*" | "+" | "[" | "{" | "(" | ")")
    int ESC_CHARACTER = TokenType.createToken("ESC_CHARACTER");
    // "\\" ("w" | "W" | "s" | "S" | "d" | "D")
    int CHAR_CLASS = TokenType.createToken("CHAR_CLASS");
    // "\\u" XXXX
    int UNICODE_CHAR = TokenType.createToken("UNICODE_CHAR");
    // "\\x" XX
    int HEX_CHAR = TokenType.createToken("HEX_CHAR");
    // "\\0" OOO
    int OCT_CHAR = TokenType.createToken("OCT_CHAR");
    // "\\c" x
    int CTRL = TokenType.createToken("CTRL");
    // "\\p" | "\\P"
    int PROPERTY = TokenType.createToken("PROPERTY");

    // e.g. "\\#" but also "\\q" which is not a valid escape actually
    int REDUNDANT_ESCAPE = TokenType.createToken("REDUNDANT_ESCAPE");

    int MINUS = TokenType.createToken("MINUS");
    int CHARACTER = TokenType.createToken("CHARACTER");

    int BAD_CHARACTER = TokenType.createToken("BAD_CHARACTER");
    int BAD_OCT_VALUE = TokenType.createToken("BAD_OCT_VALUE");
    int BAD_HEX_VALUE = TokenType.createToken("BAD_HEX_VALUE");

    int COMMENT = TokenType.createToken("COMMENT");
    int OPTIONS_ON = TokenType.createToken("OPTIONS_ON");
    int OPTIONS_OFF = TokenType.createToken("OPTIONS_OFF");

    // (?P<name>...
    int PYTHON_NAMED_GROUP = TokenType.createToken("PYTHON_NAMED_GROUP");
    // (?P=name)
    int PYTHON_NAMED_GROUP_REF = TokenType.createToken("PYTHON_NAMED_GROUP_REF");
    // (?(id/name)yes-pattern|no-pattern)
    int PYTHON_COND_REF = TokenType.createToken("PYTHON_COND_REF");
  
    // (?<name>...
    int RUBY_NAMED_GROUP = TokenType.createToken("RUBY_NAMED_GROUP");

    // (?'name'...
    int RUBY_QUOTED_NAMED_GROUP = TokenType.createToken("RUBY_QUOTED_NAMED_GROUP");

    int INVALID_CHARACTER_ESCAPE_TOKEN = TokenType.createToken("INVALID_CHARACTER_ESCAPE_TOKEN");
    int VALID_STRING_ESCAPE_TOKEN = TokenType.createToken("VALID_STRING_ESCAPE_TOKEN");
    int INVALID_UNICODE_ESCAPE_TOKEN = TokenType.createToken("INVALID_UNICODE_ESCAPE_TOKEN");

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
