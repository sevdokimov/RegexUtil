var Range = ace.require("ace/range").Range;

function installRegexpFindDependency(regexpEditor, textEditor, matchesResult, groupTable) {
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
      
      var currentMatchResult
      var currentMatchResultRange
      var lastMatchResult
      
      var cursorPos = session.getSelection().getCursor()
      
      while (r = regex.exec(text)) {
        if (r[0].length == 0) {
          break // regexp is empty
        }
        
        var startPos = session.getDocument().indexToPosition(r.index)
        var endPos = session.getDocument().indexToPosition(r.index + r[0].length)
        
        var range = Range.fromPoints(startPos, endPos)

        drawLineMarker(markerLayer, html, range, session, (matchCount & 1) ? 'matched2' : 'matched1', config)
        
        matchCount++
        lastMatchResult = r
        
        if (!regex.global) {
          break
        }
        else {
          if (range.contains(cursorPos.row, cursorPos.column)) {
            currentMatchResult = r
            currentMatchResultRange = range
          }
        }
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
      
      if (groupTable) {
        if (matchCount == 0) {
          $('td:last-child', groupTable).html("<span class='spec'>No matches found<span>")
        }
        else {
          if (matchCount == 1) {
            currentMatchResult = lastMatchResult
            currentMatchResultRange = range
          }

          if (currentMatchResultRange) {
            drawLineMarker(markerLayer, html, currentMatchResultRange, session, 'currentMatchResult', config)
          }

          if (currentMatchResult) {
            var groupRows = groupTable.groupRows
            if (groupRows) {
              for (var i = 0; i < groupRows.length; i++) {
                var tr = groupRows[i]

                var s = ""
                if (currentMatchResult) {
                  s = currentMatchResult[i] || ""
                }
                $('td:last-child', tr).empty().append($("<span class='groupText'></span>").text(s))
              }
            }
          }
          else {
            $('td:last-child', groupTable).html("<span class='spec'>please, move the cursor to matched text<span>")
          }
          
        }
      }
    }
  }

  textEditor.getSession().addDynamicMarker(marker)

  textEditor.on("change", function() {
    textEditor.onChangeBackMarker()
  })
  textEditor.getSession().selection.on('changeCursor', function() {
    textEditor.onChangeBackMarker()
  })

  function fixGroupCount() {
    if (groupTable) {
      var bracketStructure = regexpEditor.session.bracketStructure

      var groupRows = groupTable.groupRows
      if (groupRows == undefined) {
        groupTable.groupRows = groupRows = []
        groupRows.push($("tr", groupTable)[0])
      }
      
      if (groupRows.length != bracketStructure.groups.length + 1) {
        if (groupRows.length > bracketStructure.groups.length + 1) {
          while (groupRows.length > bracketStructure.groups.length + 1) {
            $(groupRows.pop()).remove()
          }
        }
        else {
          for (var i = groupRows.length; i < bracketStructure.groups.length + 1; i++) {
            var e = $("<tr><td>#" + i + "</td><td></td></tr>")
            groupTable.append(e)
            groupRows.push(e)
          }
        }
      }
    }
  }
  
  regexpEditor.addRegexChangeListener(function () {
    textEditor.onChangeBackMarker()
    fixGroupCount()
  })

  fixGroupCount()

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
