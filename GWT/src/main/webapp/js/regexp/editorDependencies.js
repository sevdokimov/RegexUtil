var Range = ace.require("ace/range").Range;

function installRegexpFindDependency(regexpEditor, textEditor) {
  var marker = {
    id: 'regexTextFindMarked',

    update: function (html, markerLayer, session, config) {
      var regex = regexpEditor.regex
      if (!regex) return

      var start = config.firstRow, end = config.lastRow;

      var text = session.getValue()

      var r;

      regex.lastIndex = 0
      
      var odd = true
      
      while (r = regex.exec(text)) {
        if (regex.global && regex.lastIndex == 0) {
          break // regexp is empty
        }
        
        var startPos = session.getDocument().indexToPosition(r.index)
        var endPos = session.getDocument().indexToPosition(r.index + r[0].length)
        
        var range = Range.fromPoints(startPos, endPos)
        
        markerLayer.drawSingleLineMarker(html,
                                         range.toScreenRange(session),
                                         odd ? 'matched1' : 'matched2', 
                                         config);
        
        odd = !odd

        if (!regex.global) break
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
