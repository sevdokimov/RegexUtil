/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ess.regexutil.gwt.psi.client.parser;

import com.ess.regexutil.gwt.psi.client.CompositePsiElement;
import com.ess.regexutil.gwt.psi.client.LeafPsiElement;
import com.ess.regexutil.gwt.psi.client.PsiElement;
import com.ess.regexutil.gwt.psi.client.PsiFile;
import com.ess.regexutil.gwt.psi.client.lexer.IElementType;
import com.ess.regexutil.gwt.psi.client.lexer.TokenSet;

/**
 * The IDEA side of a custom language parser. Provides lexing results to the
 * plugin and allows the plugin to build the AST tree.
 *
 * @see PsiParser
 * @see PsiElement
 */
public interface PsiBuilder {
  /**
   * Returns the complete text being parsed.
   *
   * @return the text being parsed
   */
  String getOriginalText();

  /**
   * Advances the lexer to the next token, skipping whitespace and comment tokens.
   */
  void advanceLexer();

  /**
   * Returns the type of current token from the lexer.
   *
   * @return the token type, or null when lexing is over.
   */
  IElementType getTokenType();

  /**
   * See what token type is in <code>steps</code> ahead
   * @param steps 0 is current token (i.e. the same {@link PsiBuilder#getTokenType()} returns)
   * @return type element which getTokenType() will return if we call advance <code>steps</code> times in a row
   */
  IElementType lookAhead(int steps);

  /**
   * See what token type is in <code>steps</code> ahead / behind
   * @param steps 0 is current token (i.e. the same {@link PsiBuilder#getTokenType()} returns)
   * @return type element ahead or behind, including whitespace / comment tokens
   */
  IElementType rawLookup(int steps);

  /**
   * See what token type is in <code>steps</code> ahead / behind current position
   * @param steps 0 is current token (i.e. the same {@link PsiBuilder#getTokenType()} returns)
   * @return offset type element ahead or behind, including whitespace / comment tokens, -1 if first token,
   * getOriginalText().getLength() at end
   */
  int rawTokenTypeStart(int steps);

  /**
   * Returns the text of the current token from the lexer.
   *
   * @return the token text, or null when the lexing is over.
   */
  String getTokenText();

  /**
   * Returns the start offset of the current token, or the file length when the lexing is over.
   *
   * @return the token offset.
   */
  int getCurrentOffset();

  /**
   * A marker defines a range in the document text which becomes a node in the AST
   * tree. The ranges defined by markers within the text range of the current marker
   * become child nodes of the node defined by the current marker.
   */
  interface Marker {
    /**
     * Creates and returns a new marker starting immediately before the start of
     * this marker and extending after its end. Can be called on a completed or
     * a currently active marker.
     *
     * @return the new marker instance.
     */
    Marker precede();

    /**
     * Drops this marker. Can be called after other markers have been added and completed
     * after this marker. Does not affect lexer position or markers added after this marker.
     */
    void drop();

    /**
     * Drops this marker and all markers added after it, and reverts the lexer position to the
     * position of this marker.
     */
    void rollbackTo();

    /**
     * Completes this marker and labels it with the specified AST node type. Before calling this method,
     * all markers added after the beginning of this marker must be either dropped or completed.
     *
     */
    void done(CompositePsiElement psiElement);

    void done(IElementType type);

    /**
     * Like {@linkplain #done(PsiElement)}, but collapses all tokens between start and end markers
     * into single leaf node of given type.
     *
     * @param type the type of the node in the AST tree.
     */
    //void collapse(int type);

    /**
     * Like {@linkplain #done(IElementType)}, but the marker is completed (end marker inserted)
     * before specified one. All markers added between start of this marker and the marker specified as end one
     * must be either dropped or completed.
     *
     * @param type   the type of the node in the AST tree.
     * @param before marker to complete this one before.
     */
    //void doneBefore(int type, Marker before);

    /**
     * Like {@linkplain #doneBefore(IElementType, Marker)}, but in addition an error element with given text
     * is inserted right before this marker's end.
     *
     * @param type         the type of the node in the AST tree.
     * @param before       marker to complete this one before.
     * @param errorMessage for error element.
     */
    //void doneBefore(int type, Marker before, String errorMessage);

    /**
     * Completes this marker and labels it as error element with specified message. Before calling this method,
     * all markers added after the beginning of this marker must be either dropped or completed.
     *
     * @param message for error element.
     */
    void error(String message);

    /**
     * Like {@linkplain #error(String)}, but the marker is completed before specified one.
     *
     * @param message for error element.
     * @param before  marker to complete this one before.
     */
    //void errorBefore(String message, Marker before);

    /**
     * Allows to define custom edge token binders instead of default ones. If any of parameters is null
     * then corresponding token binder won't be changed (keeping previously set or default token binder).
     * It is an error to set right token binder for not-done marker.
     *
     * @param left  new left edge token binder.
     * @param right new right edge token binder.
     */
    //void setCustomEdgeTokenBinders(@Nullable WhitespacesAndCommentsBinder left, @Nullable WhitespacesAndCommentsBinder right);
  }

  /**
   * Creates a marker at the current parsing position.
   *
   * @return the new marker instance.
   */
  Marker mark();

  /**
   * Adds an error marker with the specified message text at the current position in the tree.
   * <br><b>Note</b>: from series of subsequent errors messages only first will be part of resulting tree.
   *
   * @param messageText the text of the error message displayed to the user.
   */
  void error(String messageText);

  /**
   * Checks if the lexer has reached the end of file.
   *
   * @return true if the lexer is at end of file, false otherwise.
   */
  boolean eof();

  PsiFile build();
}
