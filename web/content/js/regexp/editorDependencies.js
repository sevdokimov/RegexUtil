var Range = ace.require("ace/range").Range;

function installRegexpFindDependency(regexpEditor, textEditor, matchesResult) {
  var marker = {
    id: 'regexTextFindMarked',

    update: function (html, markerLayer, session, config) {
      var regex = regexpEditor.regex
      if (!regex) return

      var start = config.firstRow, end = config.lastRow;

      var text = session.getValue()

      var r;

      regex.lastIndex = 0
      
      var matchCount = 0
      
      while (r = regex.exec(text)) {
        if (r[0].length == 0) {
          break // regexp is empty
        }
        
        var startPos = session.getDocument().indexToPosition(r.index)
        var endPos = session.getDocument().indexToPosition(r.index + r[0].length)
        
        var range = Range.fromPoints(startPos, endPos)

        drawLineMarker(markerLayer, html, range, session, (matchCount & 1) ? 'matched2' : 'matched1', config)
        
        matchCount++
        
        if (!regex.global) break
      }
      
      if (matchesResult) {
        var matchResultText
        
        if (matchCount == 0) {
          matchResultText = "No matches"
        }
        else {
          if (regex.global) {
            matchResultText = matchCount + " matches found"
          }
          else {
            matchResultText = ""
          }
        }
        
        matchesResult.text(matchResultText)
      }
    }
  }

  textEditor.getSession().addDynamicMarker(marker)

  textEditor.on("change", function() {
    textEditor.onChangeBackMarker()
  })

  regexpEditor.addRegexChangeListener(function () {
    textEditor.onChangeBackMarker()
  })

  //regexpEditor.getSession().selection.on("changeSelection", function () {
  //  if (!regexpEditor.curOp.command.name) {
  //    if (!regexpEditor.regexp_state_notification) {
  //      regexEditorStateMayBeChanged(regexpEditor, textEditor)
  //    }
  //  }
  //  else {
  //    regexpEditor.regexp_state_notification = true
  //  }
  //})
  //
  //regexpEditor.commands.on("afterExec", function () {
  //  if (regexpEditor.regexp_state_notification) {
  //    regexpEditor.regexp_state_notification = false
  //    regexEditorStateMayBeChanged(regexpEditor, textEditor)
  //  }
  //})
  textEditor.onChangeBackMarker()
}
