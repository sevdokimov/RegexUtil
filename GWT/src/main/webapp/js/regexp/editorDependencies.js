function installRegexpFindDependency(regexpEditor, textEditor) {
  regexpEditor.on("changeSelection", function(delta, x) {
    textEditor.insert("1")
  })

}