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

import com.ess.regexutil.gwt.psi.client.*;
import com.ess.regexutil.gwt.psi.client.lexer.ElementTypes;
import com.ess.regexutil.gwt.psi.client.lexer.IElementType;
import com.ess.regexutil.gwt.psi.client.lexer.Lexer;
import com.ess.regexutil.gwt.psi.client.lexer.TokenSet;

import java.util.*;

/**
 * @author max
 */
public class PsiBuilderImpl implements PsiBuilder {
  private int[] myLexStarts;
  private IElementType[] myLexTypes;
  private int myCurrentLexeme;

  private MarkerImpl currentMarker;

  private final Lexer myLexer;
  private final TokenSet myWhitespaces;
  private final TokenSet myComments;

  private final String myText;

  private int myLexemeCount = 0;

  public PsiBuilderImpl(TokenSet whiteSpaces,
                        TokenSet comments,
                        Lexer lexer,
                        String text) {
    myText = text;
    myLexer = lexer;

    myWhitespaces = whiteSpaces;
    myComments = comments;

    cacheLexemes();

    currentMarker = mark();
  }

  private void cacheLexemes() {
    int approxLexCount = myText.length();

    myLexStarts = new int[approxLexCount];
    myLexTypes = new IElementType[approxLexCount];

    myLexer.start(myText);
    int i = 0;
    int offset = 0;
    while (true) {
      IElementType type = myLexer.getTokenType();
      if (type == null) break;

      if (i >= myLexTypes.length - 1) {
        resizeLexemes(i * 3 / 2);
      }
      int tokenStart = myLexer.getTokenStart();
      if (tokenStart < offset) {
        final StringBuilder sb = new StringBuilder();
        IElementType tokenType = myLexer.getTokenType();
        sb.append("Token sequence broken")
          .append("\n  this: '").append(myLexer.getTokenText()).append("' (").append(tokenType).append(':')
          .append(") ").append(tokenStart).append(":").append(myLexer.getTokenEnd());
        if (i > 0) {
          final int prevStart = myLexStarts[i - 1];
          sb.append("\n  prev: '").append(myText.subSequence(prevStart, offset)).append("' (").append(myLexTypes[i - 1]).append(':')
            .append(") ").append(prevStart).append(":").append(offset);
        }
        final int quoteStart = Math.max(tokenStart - 256, 0);
        final int quoteEnd = Math.min(tokenStart + 256, myText.length());
        sb.append("\n  quote: [").append(quoteStart).append(':').append(quoteEnd)
          .append("] '").append(myText.subSequence(quoteStart, quoteEnd)).append('\'');
        throw new RuntimeException(sb.toString());
      }
      myLexStarts[i] = offset = tokenStart;
      myLexTypes[i] = type;
      i++;
      myLexer.advance();
    }

    myLexStarts[i] = myText.length();

    myLexemeCount = i;
  }

  //@Override
  ////@Nullable
  //public LighterASTNode getLatestDoneMarker() {
  //  int index = myProduction.size() - 1;
  //  while (index >= 0) {
  //    ProductionMarker marker = myProduction.get(index);
  //    if (marker instanceof DoneMarker) return ((DoneMarker)marker).myStart;
  //    --index;
  //  }
  //  return null;
  //}

  //@Override
  //public int getElementType(int lexemIndex) {
  //  return lexemIndex < myLexemeCount && lexemIndex >= 0 ? myLexTypes[lexemIndex] : 0;
  //}

  @Override
  public String getOriginalText() {
    return myText;
  }

  @Override
  public IElementType getTokenType() {
    if (eof()) return null;

    //if (myRemapper != null) {
    //  IElementType type = myLexTypes[myCurrentLexeme];
    //  type = myRemapper.filter(type, myLexStarts[myCurrentLexeme], myLexStarts[myCurrentLexeme + 1], myLexer.getBufferSequence());
    //  myLexTypes[myCurrentLexeme] = type; // filter may have changed the type
    //  return type;
    //}
    return myLexTypes[myCurrentLexeme];
  }

  //@Override
  //public void setTokenTypeRemapper(final ITokenTypeRemapper remapper) {
  //  myRemapper = remapper;
  //}

  //@Override
  //public void remapCurrentToken(IElementType type) {
  //  myLexTypes[myCurrentLexeme] = type;
  //}

//  @Nullable
  @Override
  public IElementType lookAhead(int steps) {
    if (eof()) {    // ensure we skip over whitespace if it's needed
      return null;
    }
    int cur = myCurrentLexeme;

    while (steps > 0) {
      ++cur;
      while (cur < myLexemeCount && whitespaceOrComment(myLexTypes[cur])) {
        cur++;
      }

      steps--;
    }

    return cur < myLexemeCount ? myLexTypes[cur] : null;
  }

  @Override
  public IElementType rawLookup(int steps) {
    int cur = myCurrentLexeme + steps;
    return cur < myLexemeCount && cur >= 0 ? myLexTypes[cur] : null;
  }

  @Override
  public int rawTokenTypeStart(int steps) {
    int cur = myCurrentLexeme + steps;
    if (cur < 0) return -1;
    if (cur >= myLexemeCount) return getOriginalText().length();
    return myLexStarts[cur];
  }

  //@Override
  //public void setWhitespaceSkippedCallback(WhitespaceSkippedCallback callback) {
  //  myWhitespaceSkippedCallback = callback;
  //}

  @Override
  public void advanceLexer() {
    if (eof()) return;
    assert currentMarker.elements.isEmpty();

    myCurrentLexeme++;
  }

  private void skipWhitespace() {
    while (myCurrentLexeme < myLexemeCount && whitespaceOrComment(myLexTypes[myCurrentLexeme])) {
      onSkip(myLexTypes[myCurrentLexeme], myLexStarts[myCurrentLexeme], myCurrentLexeme + 1 < myLexemeCount ? myLexStarts[myCurrentLexeme + 1] : myText.length());
      myCurrentLexeme++;
    }
  }

  private void onSkip(IElementType type, int start, int end) {
    //if (myWhitespaceSkippedCallback != null) {
    //  myWhitespaceSkippedCallback.onSkip(type, start, end);
    //}
  }

  @Override
  public int getCurrentOffset() {
    if (eof()) return getOriginalText().length();
    return myLexStarts[myCurrentLexeme];
  }

  @Override
  //@Nullable
  public String getTokenText() {
    if (eof()) return null;

    return myText.substring(myLexStarts[myCurrentLexeme], myLexStarts[myCurrentLexeme + 1]);
  }

  private void resizeLexemes(final int newSize) {
    int count = Math.min(newSize, myLexTypes.length);
    int[] newStarts = new int[newSize + 1];
    System.arraycopy(myLexStarts, 0, newStarts, 0, count);
    myLexStarts = newStarts;

    IElementType[] newTypes = new IElementType[newSize];
    System.arraycopy(myLexTypes, 0, newTypes, 0, count);
    myLexTypes = newTypes;
  }

  private boolean whitespaceOrComment(IElementType token) {
    return myWhitespaces.contains(token) || myComments.contains(token);
  }

  @Override
  public MarkerImpl mark() {
    assert myCurrentLexeme == currentMarker.firstLexem || currentMarker.elements.size() > 0;

    MarkerImpl res = new MarkerImpl(currentMarker);
    currentMarker = res;
    return res;
  }

  @Override
  public void error(String messageText) {
    if (currentMarker.elements.isEmpty() || currentMarker.elements.get(0).getType() != ElementTypes.ERROR_ELEMENT) {
      currentMarker.elements.add(new ErrorPsiElement(messageText));
    }
  }


  @Override
  public final boolean eof() {
    skipWhitespace();
    return myCurrentLexeme >= myLexemeCount;
  }

  public PsiFile build() {
    assert currentMarker.parent == null;
    assert eof();

    PsiFile file = new PsiFile(getOriginalText());

    for (PsiElement element : currentMarker.elements) {
      file.addElement(element);
    }

    if (file.getLength() != getOriginalText().length()) {
      throw new AssertionError();
    }

    return file;
  }

  private class MarkerImpl implements Marker {

    private MarkerImpl parent;

    private int firstLexem = myCurrentLexeme;
    //private int firstUndoneLexem = myCurrentLexeme;

    private List<PsiElement> elements = new ArrayList<PsiElement>();

    private MarkerImpl(MarkerImpl parent) {
      this.parent = parent;
    }

    @Override
    public MarkerImpl precede() {
      if (currentMarker != this) throw new UnsupportedOperationException();

      MarkerImpl res = new MarkerImpl(parent);

      parent = res;

      return res;
    }

    @Override
    public void drop() {
      assert currentMarker == this;

      parent.elements.addAll(elements);
      //parent.firstUndoneLexem = firstUndoneLexem;
      currentMarker = parent;
    }

    @Override
    public void rollbackTo() {
      assert currentMarker == this;
      myCurrentLexeme = firstLexem;
      currentMarker = parent;
    }

    @Override
    public void done(IElementType type) {
      done(new CompositePsiElement(type));
    }

    @Override
    public void done(CompositePsiElement element) {
      assert currentMarker == this;

      for (PsiElement psiElement : elements) {
        element.addElement(psiElement);
      }

      parent.elements.add(element);
      currentMarker = parent;
    }

    @Override
    public void error(String message) {
      throw new UnsupportedOperationException();
    }
  }
}
