package com.ess.regexutil.gwt.psi.client.lexer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sergey Evdokimov
 */
public abstract class TokenSet {

  public static final TokenSet EMPTY = new TokenSet() {
    @Override
    public boolean contains(IElementType element) {
      return false;
    }

    @Override
    public IElementType[] getElements() {
      return new IElementType[0];
    }
  };

  public abstract boolean contains(IElementType element);

  public abstract IElementType[] getElements();

  public static TokenSet create(final IElementType... elements) {
    return new TokenSet() {
      private boolean[] m;

      {
        m = new boolean[IElementType.getMaxElementTypeIndex()];

        for (IElementType element : elements) {
          m[element.getIndex()] = true;
        }
      }

      @Override
      public boolean contains(IElementType element) {
        if (element == null) return false;

        int index = element.getIndex();
        return index < m.length && m[index];
      }

      @Override
      public IElementType[] getElements() {
        return elements;
      }
    };
  }

  // todo implement
  //public static TokenSet create(int element);

  public static TokenSet orSet(TokenSet... tokenSets) {
    Set<IElementType> set = new HashSet<IElementType>();

    for (TokenSet tokenSet : tokenSets) {
      Collections.addAll(set, tokenSet.getElements());
    }

    IElementType[] allElements = set.toArray(new IElementType[set.size()]);

    return create(allElements);
  }
}
