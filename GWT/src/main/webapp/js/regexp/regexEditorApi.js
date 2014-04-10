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

  regexpEditor.setFlags = function(flags) {
    regexpEditor.regex_flags = flags
    onRegexChange()
  }

  onRegexChange()
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