package com.ess.regexutil.gwt.psi.client.lexer;

/**
 * @author Sergey Evdokimov
 */
public abstract class TokenSet {

    public static final TokenSet EMPTY_TOKEN_SET = new TokenSet() {
        @Override
        public boolean contains(int element) {
            return false;
        }

        @Override
        public int[] getElements() {
            return new int[0];
        }
    };

    private int[] cachedElements;

    public abstract boolean contains(int element);

    public abstract int[] getElements();

    public static TokenSet create(final int ... elements) {
        return new TokenSet() {
            private boolean[] m;

            {
                m = new boolean[TokenType.getTokenCounts()];

                for (int element : elements) {
                    m[element] = true;
                }
            }

            @Override
            public boolean contains(int element) {
                return element < m.length && m[element];
            }

            @Override
            public int[] getElements() {
                return elements;
            }

        };
    }

    // todo implement
    //public static TokenSet create(int element);

    public static TokenSet orSet(TokenSet ... tokenSets) {
        boolean[] m = new boolean[TokenType.getTokenCounts()];

        int count = 0;

        for (TokenSet tokenSet : tokenSets) {
            for (int element : tokenSet.getElements()) {
                if (!m[element]) {
                    count++;
                    m[element] = true;
                }
            }
        }

        int[] allElements = new int[count];
        int k = 0;
        for (int i = 1; i < m.length; i++) {
            if (m[i]) {
                allElements[k++] = i;
            }
        }

        return create(allElements);
    }
}
