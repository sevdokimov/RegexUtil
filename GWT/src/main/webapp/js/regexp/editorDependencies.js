var Range = ace.require("ace/range").Range;

function installRegexpFindDependency(regexpEditor, textEditor) {
  var marker = {
    id: 'regexTextFindMarked',

    setRegexp: function (regExp) {
      this.regExp = regExp;
      this.cache = [];
    },

    update: function (html, markerLayer, session, config) {
      if (!this.regExp) return

      var start = config.firstRow, end = config.lastRow;

      var text = session.getValue()

      var r;

      this.regExp.lastIndex = 0
      
      var odd = true
      
      while (r = this.regExp.exec(text)) {
        if (this.regExp.lastIndex == 0) {
          break // regexp is empty
        }
        
        var startPos = session.getDocument().indexToPosition(r.index)
        var endPos = session.getDocument().indexToPosition(this.regExp.lastIndex)
        
        var range = Range.fromPoints(startPos, endPos)
        
        markerLayer.drawSingleLineMarker(html,
                                         range.toScreenRange(session),
                                         odd ? 'matched1' : 'matched2', 
                                         config);
        
        odd = !odd
      }
    }
  }

  textEditor.my_marker = marker
  textEditor.getSession().addDynamicMarker(marker)

  textEditor.on("change", function() {
    textEditor.onChangeBackMarker()
  })

  regexpEditor.on("change", function () {
    regexChanged(regexpEditor, textEditor)
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

  regexChanged(regexpEditor, textEditor)
}

function regexChanged(regexpEditor, textEditor) {
  //var newText = regexpEditor.getValue()
  //if (newText != regexpEditor.my_old_text) {
  //  regexpEditor.my_old_text = newText
  //  hasChanges = true
  //}
  //var newSelRange = regexpEditor.getSelectionRange()
  //if (hasChanges || !regexpEditor.my_old_sel_range || !newSelRange.isEqual(regexpEditor.my_old_sel_range)) {
  //  regexpEditor.my_old_sel_range = newSelRange
  //  hasChanges = true
  //}
  //if (!hasChanges) return;
  // State was changed
  var regex = null;
  try {
    regex = new RegExp(regexpEditor.getValue(), "g");
  }
  catch (e) {
  }
  
  textEditor.my_marker.setRegexp(regex)
  textEditor.onChangeBackMarker()
  
  console.log(regexpEditor.getValue())
}
