function runTests() {
  testRegexHighlighting("\\d+", ["charClassEsc", "quantifier"])
  
  testRegexHighlighting("+", ["error"])
  testRegexHighlighting("()+", ["openBracket", "closedBracket", "quantifier"])
  testRegexHighlighting("\\b+", ["assertion", "error"])
  testRegexHighlighting("(?:\\b)", ["openBracket", "assertion", "closedBracket"])

  testRegexHighlighting("\\i", ["error.incorrectEsc#\\i"])
  testRegexHighlighting("\\:", ['escapeSymbol#\\', 'escapedSymbol#:'])
  
  testRegexHighlighting("a]", ["defText"])
  testRegexHighlighting("[]", ["error"], true)
  testRegexHighlighting("[^]", ["error"], true)
  testRegexHighlighting("}+", ["defText", "quantifier"])

  testRegexHighlighting("[\\182403\\040-z]", ["charClassStart", 'numEsc', 'numEsc', "charClassRange#-", 'charClassAtom', "charClassEnd"])
  testRegexHighlighting("[\\:]", ["charClassStart", 'escapeSymbol', 'escapedSymbol', "charClassEnd"])
  testRegexHighlighting("[-a]", ["charClassStart", 'charClassAtom', 'charClassAtom', "charClassEnd"])
  testRegexHighlighting("[a-]", ["charClassStart", 'charClassAtom', 'charClassAtom', "charClassEnd"])
  testRegexHighlighting("[-]", ["charClassStart", 'charClassAtom', "charClassEnd"])
  testRegexHighlighting("[-]", ["charClassStart", 'charClassAtom', "charClassEnd"])
  testRegexHighlighting("[a-\\x99-b]", ["charClassStart", 'charClassAtom#a', 'charClassRange', 'numEsc', "charClassAtom#-", "b", "charClassEnd"])
  testRegexHighlighting("[a-\\u9999-]", ["charClassStart", 'charClassAtom#a', 'charClassRange', 'numEsc', "charClassAtom#-", "charClassEnd"])
  testRegexHighlighting("[a-\\w]", ["charClassStart", 'a', 'charClassAtom#-', 'charClassEsc', "charClassEnd"])
  testRegexHighlighting("[\\w-a]", ["charClassStart", 'charClassEsc', 'charClassAtom#-', 'a', "charClassEnd"])
  testRegexHighlighting("[\\i\\u\\x]", ["charClassStart", 'escapeSymbol#\\', 'escapedSymbol#i','escapeSymbol#\\', 'u', 'escapeSymbol#\\', 'x', "charClassEnd"])
  testRegexHighlighting("[a-\\x]", ["charClassStart", 'charClassAtom#a', 'charClassRange', 'escapeSymbol#\\', 'x', "charClassEnd"])
}