function assert(condition, message) {
  if (!condition) {
    throw message || "Assertion failed";
  }
}

function drawLineMarker(markerLayer, html, range, session, clazz, config) {
  if (range.start.row == range.end.row) {
    markerLayer.drawSingleLineMarker(html, range.toScreenRange(session), clazz, config);
  }
  else {
    markerLayer.drawMultiLineMarker(html, range.toScreenRange(session), clazz, config);
  }
}

function saveTokenIteratorState(itr) {
  return {
    row: itr.$row,
    tokenIndex: itr.$tokenIndex
  }
}

function loadTokenIteratorState(itr, state) {
  itr.$row = state.row
  itr.$rowTokens = itr.$session.getTokens(state.row)
  itr.$tokenIndex = state.tokenIndex
}

function getUrlParam(name){
  if(name=(new RegExp('[?&]'+encodeURIComponent(name)+'=([^&]*)')).exec(location.search)) {
    return decodeURIComponent(name[1]);
  }
}

function sendNotification(listeners, e) {
  var len = listeners.length
  for (var i = 0; i < len; i++) {
    listeners[i](e);
  }
}

function copyToClipboard(text) {
  window.prompt("Copy to clipboard: Ctrl+C, Enter", text);
}

function customizeEditor(editor) {
  editor.commands.removeCommand('indent')
  editor.commands.removeCommand('outdent')
  editor.commands.removeCommand('centerselection')
  editor.commands.removeCommand('gotoline')
}