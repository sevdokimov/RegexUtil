function testRegexHighlighting(text, tokens, ignoreCompilationIncompatibility) {
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

  if (sucess && !ignoreCompilationIncompatibility) {
    var failedRegexCompilation
    
    try {
      new RegExp(text)
      failedRegexCompilation = false
    }
    catch (e) {
      failedRegexCompilation = true
    }
    
    if (failedRegexCompilation != hasErrorToken) {
      var message
      
      if (failedRegexCompilation) {
        message = "Uncompiled regex shown without errors: " + text + "<br><br>"
      }
      else {
        message = "Correct regex shown with errors: " + text + "<br><br>"
      }
      
      document.test_errors += message
    }
  }
  
  if (!sucess) {
    var actualTokens = ""
    
    for (i = 0; i < t.length; i++) {
      if (actualTokens.length > 0) {
        actualTokens += ", "
      }
      actualTokens += t[i].type + '#' + t[i].value
    }

    var errorMsg = "<table class='testError'>" +
                   "<tr>" +
                   "  <td>Regexp</td>" +
                   "  <td>" + text + "</td>" +
                   "</tr>" +
                   "<tr>" +
                   "  <td>Expected tokens</td>" +
                   "  <td>" + tokens.join(", ") + "</td>" +
                   "</tr>" +
                   "<tr>" +
                   "  <td>Actual tokens</td>" +
                   "  <td>" + actualTokens + "</td>" +
                   "</tr>" +
                   "</table>" +
                   "<br><br>"
    
    document.test_errors += errorMsg
  }
}