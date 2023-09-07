package com.ess.regexutil.ideaplugin;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

public abstract class MyBasePlatformTestCase extends BasePlatformTestCase {

    static {
        System.setProperty("idea.debug", "true");
    }

    /**
     * Gradle doesn't see test classes without a method with @Test annotation.
     */
    @SuppressWarnings("JUnit4AnnotatedMethodInJUnit3TestCase")
    @Test
    public void dummyTest() {

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MatchingProcessor.REHIGHLIGHT_DELAY = 0;
    }
}
