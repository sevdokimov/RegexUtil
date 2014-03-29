function testRegexHighlighting(text, tokens) {
  testHighlighting("ace/mode/regexp", text, tokens)
}

function testHighlighting(mode, text, tokens) {
    var Mode = require(mode).Mode;

    var m = new Mode()

  var Tokenizer = require("ace/tokenizer").Tokenizer;
    var tokenizer = new Tokenizer(new m.HighlightRules().$rules);

    var t = tokenizer.getLineTokens(text).tokens

    var sucess = tokens.length == t.length

    if (sucess) {
        for (var i = 0; i < tokens.length; i++) {
            var tmpl = tokens[i]
            var idx = tmpl.indexOf('#')

            if (idx == -1) {
                sucess = (t[i].type == tmpl || t[i].value == tmpl);
            }
            else {
                sucess = (t[i].type = tmpl.substr())
            }

            if (!sucess) break
        }
    }

    assert(z)
}