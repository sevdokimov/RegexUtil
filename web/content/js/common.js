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