define('ess/regex/regex_api', [], function(require, exports, module) {
  "use strict";

  var TokenIterator = require('ace/token_iterator').TokenIterator
  var Range = ace.require("ace/range").Range;
  var RelatedElementMarker = require('ess/regex/related_elements_marker').RelatedElementMarker
  var MyTokenIterator = require("ess/regex/my_token_iterator").MyTokenIterator;


  function installRegexEditorApi(regexpEditor) {
    var regex_change_listeners = []

    regexpEditor.addRegexChangeListener = function (listener) {
      regex_change_listeners.push(listener)
    }

    var onRegexChange = function () {
      var regexText = regexpEditor.getValue()

      var flags = regexpEditor.regex_flags
      if (!flags) flags = ""

      if (regexText == regexpEditor.old_regex_text && flags == regexpEditor.regex_old_flags) return

      regexpEditor.old_regex_text = regexText
      regexpEditor.regex_old_flags = flags

      var regex = null;
      try {
        regex = new RegExp(regexText, flags);
      }
      catch (e) {
      }

      regexpEditor.regex = regex

      var len = regex_change_listeners.length
      for (var i = 0; i < len; i++) {
        regex_change_listeners[i]();
      }
    }

    regexpEditor.on("change", function () {
      onRegexChange()
    })

    regexpEditor.setFlags = function (flags) {
      regexpEditor.regex_flags = flags
      onRegexChange()
    }

    onRegexChange()

    installRegexpHighlighter(regexpEditor)
  }

  function installFlagsCheckboxListener(regexpEditor, checkboxes) {

    var rereadFlags = function () {
      var flags = ""

      for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].checked) {
          flags += $(checkboxes[i]).attr('flagValue')
        }
      }

      regexpEditor.setFlags(flags)
    }

    checkboxes.each(function () {
      $(this).change(function () {
        rereadFlags()
      })
    })

    rereadFlags()
  }

  function installRegexpHighlighter(regexpEditor) {
    regexpEditor.getSession().addDynamicMarker(new MatchedBracketMarket(regexpEditor))
    regexpEditor.getSession().addDynamicMarker(new InvalidBracketMarker())
    regexpEditor.getSession().addDynamicMarker(new RelatedElementMarker(regexpEditor), true)

    regexpEditor.on("change", function () {
      regexpEditor.onChangeBackMarker()
      regexpEditor.onChangeFrontMarker()
    })

    regexpEditor.on("focus", function () {
      regexpEditor.onChangeBackMarker()
      regexpEditor.onChangeFrontMarker()
    })

    regexpEditor.on("blur", function () {
      regexpEditor.onChangeBackMarker()
      regexpEditor.onChangeFrontMarker()
    })

    regexpEditor.getSession().selection.on('changeCursor', function () {
      regexpEditor.onChangeBackMarker()
      regexpEditor.onChangeFrontMarker()
    })
  }

  function MatchedBracketMarket(regexpEditor) {
    this.openBrackets = ['openBracket', 'charClassStart']

    this.parentBracket = {
      openBracket: 'closedBracket',
      closedBracket: 'openBracket',
      charClassStart: 'charClassEnd',
      charClassEnd: 'charClassStart'
    }

    this.update = function (html, markerLayer, session, config) {
      if (!regexpEditor.isFocused()) return

      var cursorPos = session.getSelection().getCursor()

      var t = session.getTokenAt(cursorPos.row, cursorPos.column + 1)

      var matchBracket

      if (!t || !(matchBracket = this.parentBracket[t.type])) {
        if (cursorPos.column == 0) {
          return
        }

        t = session.getTokenAt(cursorPos.row, cursorPos.column)

        if (!t || !(matchBracket = this.parentBracket[t.type])) {
          return
        }
      }

      var itr = new TokenIterator(session, cursorPos.row, t.start + 1)
      var currentToken;

      var forward = this.openBrackets.indexOf(t.type) > -1

      var range = 0
      while (true) {
        if (forward) {
          itr.stepForward()
        }
        else {
          itr.stepBackward()
        }

        currentToken = itr.getCurrentToken();
        if (!currentToken) {
          return
        }

        if (currentToken.type == matchBracket) {
          if (range == 0) {
            break
          }

          range--
        }
        else if (currentToken.type == t.type) {
          range++
        }
      }

      var firstBracketRange = new Range(cursorPos.row, t.start, cursorPos.row, t.start + t.value.length)
      markerLayer.drawSingleLineMarker(html,
                                       firstBracketRange.toScreenRange(session),
                                       'matchedBracket',
                                       config);

      var matchedBracketRow = itr.getCurrentTokenRow()
      var matchedBracketColumn = itr.getCurrentTokenColumn()
      var secondBracketRange = new Range(matchedBracketRow, matchedBracketColumn, matchedBracketRow,
                                         matchedBracketColumn + currentToken.value.length)

      markerLayer.drawSingleLineMarker(html,
                                       secondBracketRange.toScreenRange(session),
                                       'matchedBracket',
                                       config);

    }
  }

  function InvalidBracketMarker() {
    this.update = function (html, markerLayer, session, config) {
      var itr = new MyTokenIterator(session, 0)

      var openBrackets = []

      var t
      while ((t = itr.getCurrentToken())) {

        if (t.type == 'charClassStart') {
          var openBrRow = itr.getCurrentTokenRow()
          var openBrColumn = itr.getCurrentTokenColumn()
          var openBrLength = t.value.length

          do {
            t = itr.stepForward()
            if (!t) {
              if (openBrRow >= config.firstRow && openBrRow <= config.lastRow) {
                var range = new Range(openBrRow, openBrColumn, openBrRow, openBrColumn + openBrLength)

                markerLayer.drawSingleLineMarker(html,
                                                 range.toScreenRange(session),
                                                 'unmatchedBracket',
                                                 config);
              }
              break
            }

            if (t.type == 'charClassEnd') {
              break
            }
          }
          while (true)
        }
        else if (t.type == 'openBracket') {
          openBrackets.push({
                              row: itr.getCurrentTokenRow(),
                              column: itr.getCurrentTokenColumn(),
                              len: t.value.length
                            })
        }
        else if (t.type == 'closedBracket') {
          if (openBrackets.length == 0) {
            markerLayer.drawSingleLineMarker(html,
                                             new Range(
                                                 itr.getCurrentTokenRow(),
                                                 itr.getCurrentTokenColumn(),
                                                 itr.getCurrentTokenRow(),
                                                 itr.getCurrentTokenColumn() + t.value.length).toScreenRange(session),
                                             'unmatchedBracket',
                                             config);
          }
          else {
            openBrackets.pop()
          }
        }

        itr.stepForward()
      }

      for (var i = 0; i < openBrackets.length; i++) {
        var unmatchedBracket = openBrackets[i];
        markerLayer.drawSingleLineMarker(html,
                                         new Range(
                                             unmatchedBracket.row,
                                             unmatchedBracket.column,
                                             unmatchedBracket.row,
                                             unmatchedBracket.column + unmatchedBracket.len).toScreenRange(session),
                                         'unmatchedBracket',
                                         config);
      }
    }
  }

  exports.installRegexEditorApi = installRegexEditorApi
  exports.installFlagsCheckboxListener = installFlagsCheckboxListener
})