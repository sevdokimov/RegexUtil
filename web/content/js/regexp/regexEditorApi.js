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

      regexpEditor.session.bracketStructure = evaluateBracketStructure(regexpEditor.session)
      
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

  function evaluateBracketStructure(session) {
    var groups = []
    var brackets = []

    var itr = new MyTokenIterator(session, 0)

    var openBrackets = []

    var t = itr.getCurrentToken()
    while (t) {
      if (t.type == 'charClassStart') {
        var br = {
          row: itr.getCurrentTokenRow(),
          column: itr.getCurrentTokenColumn(),
          end: itr.getCurrentTokenColumn() + t.value.length
        }
        
        brackets.push(br)

        while (t = itr.stepForward()) {
          if (t.type == 'charClassEnd') {
            var closedBr = {
              row: itr.getCurrentTokenRow(),
              column: itr.getCurrentTokenColumn(),
              end: itr.getCurrentTokenColumn() + 1
            }
            
            closedBr.pair = br
            br.pair = closedBr
            
            brackets.push(closedBr)
            break
          }
        }
      }
      else if (t.type == 'openBracket') {
        br = {
          row: itr.getCurrentTokenRow(),
          column: itr.getCurrentTokenColumn(),
          end: itr.getCurrentTokenColumn() + t.value.length
        }
        brackets.push(br)
        openBrackets.push(br)

        if (t.value == '(') {
          br.captureGroup = true
        }
      }
      else if (t.type == 'closedBracket') {
        closedBr = {
          row: itr.getCurrentTokenRow(),
          column: itr.getCurrentTokenColumn(),
          end: itr.getCurrentTokenColumn() + 1
        }
        
        brackets.push(closedBr)
        
        if (openBrackets.length > 0) {
          br = openBrackets.pop()
          
          br.pair = closedBr
          closedBr.pair = br
          
          if (br.captureGroup) {
            groups.push(br)
          }
        }
      }

      t = itr.stepForward()
    }
    
    return {
      groups: groups,
      brackets: brackets
    }
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
    this.update = function (html, markerLayer, session, config) {
      if (!regexpEditor.isFocused()) return
      if (!session.bracketStructure) return

      var cursorPos = session.getSelection().getCursor()
      var caretColumn = cursorPos.column
      
      var brackets = session.bracketStructure.brackets
      
      var bracketUnderCaret
      
      for (var i = 0; i < brackets.length; i++) {
        var br = brackets[i]
        
        if (br.end < caretColumn) continue
        
        if (cursorPos.row < br.row) continue
        if (cursorPos.row > br.row) break

        if (caretColumn < br.column) break

        if (br.pair) {
          bracketUnderCaret = br
        }
      }

      if (bracketUnderCaret) {
        var row = bracketUnderCaret.row
        if (row >= config.firstRow && row <= config.lastRow) {
          var range = new Range(row, bracketUnderCaret.column, row, bracketUnderCaret.end)
          markerLayer.drawSingleLineMarker(html, range.toScreenRange(session), 'matchedBracket', config);
        }
        
        var pairRow = bracketUnderCaret.pair.row
        if (pairRow >= config.firstRow && pairRow <= config.lastRow) {
          range = new Range(row, bracketUnderCaret.pair.column, row, bracketUnderCaret.pair.end)
          markerLayer.drawSingleLineMarker(html, range.toScreenRange(session), 'matchedBracket', config);
        }
      }
    }
  }

  function InvalidBracketMarker() {
    this.update = function (html, markerLayer, session, config) {
      if (!session.bracketStructure) return
      
      var brackets = session.bracketStructure.brackets
      
      for (var i = 0; i < brackets.length; i++) {
        var br = brackets[i]
        if (!br.pair) {
          var row = br.row
          if (row >= config.firstRow && row <= config.lastRow) {
            var range = new Range(row, br.column, row, br.end)
            markerLayer.drawSingleLineMarker(html, range.toScreenRange(session), 'unmatchedBracket', config);
          }
        }
      }
    }
  }

  exports.installRegexEditorApi = installRegexEditorApi
  exports.installFlagsCheckboxListener = installFlagsCheckboxListener
})