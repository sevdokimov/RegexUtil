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

/**
 * @author max
 */
public class PsiBuilderImpl implements PsiBuilder {
  private int[] myLexStarts;
  private IElementType[] myLexTypes;
  private int myCurrentLexeme;

  private final Lexer myLexer;
  private final TokenSet myWhitespaces;
  private final TokenSet myComments;

  private final String myText;

  private int myLexemeCount = 0;

  private Node myFirstNode;
  private Node myLastNode;

  public PsiBuilderImpl(TokenSet whiteSpaces,
                        TokenSet comments,
                        Lexer lexer,
                        String text) {
    myText = text;
    myLexer = lexer;

    myWhitespaces = whiteSpaces;
    myComments = comments;

    cacheLexemes();
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
    MarkerImpl res = new MarkerImpl(myCurrentLexeme);
    addToEnd(res);
    return res;
  }

  @Override
  public void error(String messageText) {
    if (myLastNode instanceof CloseMarker) {
      if (((CloseMarker)myLastNode).myPsiElement.getElementType() == ElementTypes.ERROR_ELEMENT) {
        return;
      }
    }

    MarkerImpl marker = new MarkerImpl(myCurrentLexeme);
    addToEnd(marker);
    CloseMarker closeMarker = new CloseMarker(new ErrorPsiElement(messageText), marker);
    addToEnd(closeMarker);
  }


  @Override
  public final boolean eof() {
    skipWhitespace();
    return myCurrentLexeme >= myLexemeCount;
  }

  public PsiFile build() {
    assert myFirstNode == null || ((MarkerImpl)myFirstNode).myCloseMarker == myLastNode;
    assert eof();

    PsiFile file = new PsiFile(getOriginalText());

    MarkerImpl fakeNode = new MarkerImpl(0);
    fakeNode.next = myFirstNode;
    myFirstNode = fakeNode;

    CloseMarker closeMarker = new CloseMarker(file, fakeNode);
    fakeNode.myCloseMarker = closeMarker;
    addToEnd(closeMarker);

    fakeNode.build();

    if (file.getLength() != getOriginalText().length()) {
      throw new AssertionError();
    }

    return file;
  }

  private class Node {
    protected Node prev;
    protected Node next;

    private boolean myDeleted;

    protected void remove() {
      assert !myDeleted;

      if (prev != null) {
        prev.next = next;
      }
      else {
        myFirstNode = next;
      }

      if (next != null) {
        next.prev = prev;
      }
      else {
        myLastNode = prev;
      }

      myDeleted = true;
    }

    protected void insertBefore(Node node) {
      assert !myDeleted;

      node.prev = prev;
      node.next = this;

      if (prev == null) {
        myFirstNode = node;
      }
      else {
        prev.next = node;
      }

      prev = node;
    }
  }

  protected void addToEnd(Node node) {
    node.prev = myLastNode;
    assert node.next == null;

    if (myLastNode != null) {
      myLastNode.next = node;
    }
    else {
      myFirstNode = node;
    }

    myLastNode = node;
  }

  private class CloseMarker extends Node {
    private final int myLexemIndex = myCurrentLexeme;

    private CompositePsiElement myPsiElement;

    private final MarkerImpl myStartMarker;

    private CloseMarker(IElementType elementType, MarkerImpl startMarker) {
      this(new CompositePsiElement(elementType), startMarker);
    }

    private CloseMarker(CompositePsiElement psiElement, MarkerImpl startMarker) {
      myPsiElement = psiElement;
      myStartMarker = startMarker;
    }
  }

  private class MarkerImpl extends Node implements Marker {

    private final int myLexemIndex;

    private CloseMarker myCloseMarker;

    MarkerImpl(int lexemIndex) {
      this.myLexemIndex = lexemIndex;
    }

    void build() {
      int i = myLexemIndex;

      for (Node node = next; node != myCloseMarker; ) {
        MarkerImpl m = (MarkerImpl)node;

        while (i < m.myLexemIndex) {
          LeafPsiElement element = new LeafPsiElement(myLexTypes[i]);
          element.setLength(myLexStarts[i + 1] - myLexStarts[i]);

          myCloseMarker.myPsiElement.addElement(element);

          i++;
        }

        m.build();
        myCloseMarker.myPsiElement.addElement(m.myCloseMarker.myPsiElement);

        i = m.myCloseMarker.myLexemIndex;
        node = m.myCloseMarker.next;
      }

      while (i < myCloseMarker.myLexemIndex) {
        LeafPsiElement element = new LeafPsiElement(myLexTypes[i]);
        element.setLength(myLexStarts[i + 1] - myLexStarts[i]);

        myCloseMarker.myPsiElement.addElement(element);

        i++;
      }
    }

    @Override
    public MarkerImpl precede() {
      MarkerImpl parent = findParent();
      assert parent == null || parent.myCloseMarker == null;

      MarkerImpl res = new MarkerImpl(myLexemIndex);
      insertBefore(res);

      return res;
    }

    @Override
    public void drop() {
      assert myCloseMarker == null;

      remove();
    }

    @Override
    public void rollbackTo() {
      if (prev == null) {
        myFirstNode = null;
      }
      else {
        prev.next = null;
      }

      myLastNode = prev;

      myCurrentLexeme = myLexemIndex;
    }

    @Override
    public void done(IElementType type) {
      done(new CompositePsiElement(type));
    }

    private MarkerImpl findParent() {
      Node node = prev;

      while (node != null) {
        if (node instanceof CloseMarker) {
          node = ((CloseMarker)node).myStartMarker.prev;
        }
        else {
          return (MarkerImpl)node;
        }
      }

      return null;
    }

    @Override
    public void done(CompositePsiElement element) {
      assert myCloseMarker == null;

      Node node = myLastNode;
      while (node != this) {
        if (node instanceof CloseMarker) {
          node = ((CloseMarker)node).myStartMarker.prev;
        }
        else {
          throw new AssertionError();
        }
      }

      CloseMarker closeMarker = new CloseMarker(element, this);
      myCloseMarker = closeMarker;
      addToEnd(closeMarker);
    }

    @Override
    public void error(String message) {
      throw new UnsupportedOperationException();
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();

      sb.append("[").append(myLexStarts[myLexemIndex]).append("..");

      if (myCloseMarker == null) {
        sb.append(")");
      }
      else {
        sb.append(myLexStarts[myCloseMarker.myLexemIndex]).append("] ");
        sb.append(getOriginalText().substring(myLexStarts[myLexemIndex], myLexStarts[myCloseMarker.myLexemIndex]));
      }
      sb.append(" ");

      return sb.toString();
    }

  }
}
