function runTests() {
  testRegexHighlighting("\\d+", ["charClassEsc", "quantifier"])
  
  testRegexHighlighting("+", ["error"])
  testRegexHighlighting("()+", ["openBracket", "closedBracket", "quantifier"])
  testRegexHighlighting("\\b+", ["assertion", "error"])
  testRegexHighlighting("(?:\\b)", ["openBracket", "assertion", "closedBracket"])
}