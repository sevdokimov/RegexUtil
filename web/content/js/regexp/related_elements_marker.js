function RelatedElementMarker(regexpEditor) {
  var TokenIterator = require('ace/token_iterator').TokenIterator
  var Range = ace.require("ace/range").Range;

  this.update = function (html, markerLayer, session, config) {
    if (!regexpEditor.isFocused()) return

    var cursorPos = session.getSelection().getCursor()

    var t = session.getTokenAt(cursorPos.row, cursorPos.column + 1)

    var matchBracket

    if (!t || (t.type != 'orSymbol')) {
      if (cursorPos.column == 0) {
        return
      }

      t = session.getTokenAt(cursorPos.row, cursorPos.column)

      if (!t || (t.type != 'orSymbol')) {
        return
      }
    }

    var itr = new TokenIterator(session, cursorPos.row, t.start + 1)
    var currentToken;
    var range = 0

    if (t.type == 'orSymbol') {
      var savedItrState = saveTokenIteratorState(itr)
      
      var lastOr = t;
      lastOr.row = cursorPos.row

      do {
        currentToken = itr.stepBackward()
        if (!currentToken) break

        if (currentToken.type == 'openBracket') {
          if (range == 0) {
            break
          }
          else {
            range--
          }
        }
        else if (currentToken.type == 'closedBracket') {
          range++
        }
        else if (range == 0 && currentToken.type == 'orSymbol') {
          var column = itr.getCurrentTokenColumn()
          var currentTokenRow = itr.getCurrentTokenRow();
          currentToken.start = column
          currentToken.row = currentTokenRow

          column += currentToken.value.length

          if (column != lastOr.start || currentTokenRow != lastOr.row) {
            drawLineMarker(markerLayer, html,
                           new Range(itr.getCurrentTokenRow(), column, lastOr.row, lastOr.start),
                           session, 'relatedToken', config)
          }

          lastOr = currentToken
        }
      } while (true)

      itr.stepForward()

      column = itr.getCurrentTokenColumn()
      if (column != lastOr.start || itr.getCurrentTokenRow() != lastOr.row) {
        drawLineMarker(markerLayer, html,
                       new Range(itr.getCurrentTokenRow(), column, lastOr.row, lastOr.start),
                       session, 'relatedToken', config)
      }

      // Go forward
      loadTokenIteratorState(itr, savedItrState)

      lastOr = t;

      do {
        currentToken = itr.stepForward()
        if (!currentToken) break

        if (currentToken.type == 'openBracket') {
          range++
        }
        else if (currentToken.type == 'closedBracket') {
          if (range == 0) {
            break
          }
          else {
            range--
          }
        }
        else if (range == 0 && currentToken.type == 'orSymbol') {
          column = itr.getCurrentTokenColumn()
          currentTokenRow = itr.getCurrentTokenRow();
          currentToken.start = column
          currentToken.row = currentTokenRow

          var markerStart = lastOr.start + lastOr.value.length
          
          if (column != markerStart || currentTokenRow != lastOr.row) {
            drawLineMarker(markerLayer, html,
                           new Range(lastOr.row, markerStart, currentTokenRow, column),
                           session, 'relatedToken', config)
          }

          lastOr = currentToken
        }
      } while (true)

      itr.stepBackward()

      var markerEnd = itr.getCurrentTokenColumn() + itr.getCurrentToken().value.length
      markerStart = lastOr.start + lastOr.value.length
      if (markerStart != markerEnd || itr.getCurrentTokenRow() != lastOr.row) {
        drawLineMarker(markerLayer, html,
                       new Range(lastOr.row, markerStart, itr.getCurrentTokenRow(), markerEnd),
                       session, 'relatedToken', config)
      }
    }
  }
}
