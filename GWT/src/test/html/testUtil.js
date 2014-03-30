function testRegexHighlighting(text, tokens) {
  var Mode = require("ace/mode/regexp").Mode;

  var m = new Mode()

  var Tokenizer = require("ace/tokenizer").Tokenizer;
  var tokenizer = new Tokenizer(new m.HighlightRules().$rules);

  var t = tokenizer.getLineTokens(text).tokens

  var sucess = tokens.length == t.length

  var hasErrorToken = false
  
  if (sucess) {
    for (var i = 0; i < tokens.length; i++) {
      var tmpl = tokens[i]

      if (t[i].type == 'error') {
        hasErrorToken = true
      }

      var idx = tmpl.indexOf('#')
      if (idx == -1) {
        sucess = (t[i].type == tmpl || t[i].value == tmpl);
      }
      else {
        sucess = (t[i].type == tmpl.substring(0, idx) && t[i].value == tmpl.substring(idx + 1))
      }

      if (!sucess) break
    }
  }

  if (sucess) {
    var failedRegexCompilation
    
    try {
      new RegExp(text)
      failedRegexCompilation = false
    }
    catch (e) {
      failedRegexCompilation = true
    }
    
    if (failedRegexCompilation != hasErrorToken) {
      document.test_errors += "failedRegexCompilation == hasErrorToken<br><br>"
    }
  }
  
  if (!sucess) {
    document.test_errors += "Incorrect parsing: <br>expexct: " + tokens.join(", ") + "<br>actual:"
    for (i = 0; i < t.length; i++) {
      document.test_errors += ", " + t[i].type + '#' + t[i].value
    }
    
    document.test_errors += "<br><br>"
  }
}