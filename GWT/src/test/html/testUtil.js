function testRegexHighlighting(mode, text, tokens) {
  testHighlighting("ace/mode/regexp", text, tokens)
}

function testHighlighting(mode, text, tokens) {
  var DebugTokenizer = require("ace/tokenizer_dev").Tokenizer;
  
}