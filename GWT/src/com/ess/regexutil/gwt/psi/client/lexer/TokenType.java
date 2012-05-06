package com.ess.regexutil.gwt.psi.client.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Evdokimov
 */
public class TokenType {

    private static final List<String> descriptions = new ArrayList<String>();

    public static final int WHITE_SPACE = createToken("WHITE_SPACE");

    public static int createToken(String description) {
        descriptions.add(description);
        return descriptions.size();
    }

    public static String description(int token) {
        return descriptions.get(token - 1);
    }

    public static int getTokenCounts() {
        return descriptions.size() + 1;
    }
}
