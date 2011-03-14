var colorDupOpt = "0123456789"; // *
var colorSkipOpt = "`~!@#%^&()_=|{"; // -
var blockOpt = ";:<>,.?/"; // +

function hilightRegex(parent, hilight) {

    parent = $('<pre style="font-family:monospace;margin:0"/>').appendTo(parent)

    var len = hilight.regex.length

    // Unpack tooltips
    for (var i = 0; i < len; i++) {
        if (typeof hilight.h[i] == 'number') {
            hilight.h[i] = hilight.h[hilight.h[i]]
        }
    }

    // Unpack colors
    var cLen = hilight.c.length
    var colors = new Array(cLen / 6);
    var k = 0;
    for ( i = 0; i < cLen; i+=6) {
        colors[k++] = '#' + hilight.c.substr(i, 6)
    }

    // Unpack default colors

    var defForeground = unpackColor(hilight.f, colors[hilight.df], len, colors)
    var defBackground = unpackColor(hilight.b, colors[hilight.db], len, colors)
    var defBold = unpackBoolean(hilight.w, hilight.dw, len)
    var defItalic = unpackBoolean(hilight.i, hilight.di, len)

    // Unpack selection highlighting
    var b = {i:0,s:hilight.s}
    var sel = new Array(len)
    i = 0;
    do {

        var curSel = {}

        curSel.b = unpackColorRegion(b, colors)
        var a = lookChar(b)
        if (a == '}') {
            b.i++
            curSel.f = unpackColorRegion(b, colors)
            a = lookChar(b)
        }
        if (a == '[') {
            b.i++
            curSel.w = unpackBooleanRegion(b)
            a = lookChar(b)
        }
        if (a == ']') {
            b.i++
            curSel.i = unpackBooleanRegion(b)
        }

        k = readDup(b, blockOpt, '+')
        if (k == null)
            break

        while (--k >= 0) {
            sel[i++] = curSel
        }

    } while (true)

    var spans = new Array(len)

    for ( i = 0; i < len; i++) {

        // Create element
        var span = $("<span/>")
        span.text(hilight.regex.charAt(i))

        span.css('color', defForeground[i]).css('background', defBackground[i])
            .css('font-weight', defBold[i] ? 'bold' : 'normal')
            .css('font-style', defItalic[i] ? 'italic' : 'normal')
        span[0].charNumber = i
        span.appendTo(parent)
        spans[i] = span

        if (hilight.h[i] != '') {
            span[0].onmousemove = onmove;
        }

        span[0].onmouseover = function(event){
            if (!event) event = window.event;

            if (event.target) {
                var i = event.target.charNumber
            } else {
                i = event.srcElement.charNumber
            }

            if (hilight.h[i] != '') {
                var panel = getHintPanel()
                panel.html(hilight.h[i])
                onmove(event)
                panel.parent().css('display', 'block')
            }

            var s = sel[i]

            paintRegion(s.f, spans, 'color')
            paintRegion(s.b, spans, 'background')
            paintRegionBoolean(s.w, defBold, spans, 'font-weight', 'bold', true)
            paintRegionBoolean(s.i, defItalic, spans, 'font-style', 'italic', true)
        }

        span[0].onmouseout = function(event){

            if (!event) event = window.event;

            if (event.target) {
                var i = event.target.charNumber
            } else {
                i = event.srcElement.charNumber
            }

            var s = sel[i]
            clearRegion(s.f, defForeground, spans, 'color')
            clearRegion(s.b, defBackground, spans, 'background')
            paintRegionBoolean(s.w, defBold, spans, 'font-weight', 'bold', false)
            paintRegionBoolean(s.i, defItalic, spans, 'font-style', 'italic', false)

            getHintPanel().parent().css('display', 'none')
        }
    }
}

function onmove(event) {
    var panel = getHintPanel()
    var popupDiv = panel.parent()

    if (!event) event = window.event;
    if (event.pageX) {
        var x = event.pageX
        var y = event.pageY
    } else {
        x = event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft
        y = event.clientY + document.body.scrollTop + document.documentElement.scrollTop
    }

    if (x + 5 + popupDiv.width() <= $('body').width()) {
        var posX = x + 5;
                
    } else if (x - 5 - popupDiv.width() > 0) {
        posX = x - 5 - popupDiv.width()
    } else {
        posX = 1;
    }

    popupDiv.css('top', (y + 5) + 'px').css('left', posX + 'px')
}

function clearRegion(m, def, spans, cssName) {
    if (m == undefined)
        return;
    for (var i = 0; i < m.length; i += 3) {
        var j = m[i]
        var k = m[i+1]
        while (--k >= 0) {
            spans[j].css(cssName, def[j])
            j++
        }
    }
}

function paintRegion(m, spans, cssName) {
    if (m == undefined)
        return;
    for (var i = 0; i < m.length; i += 3) {
        var j = m[i]
        var k = m[i+1]
        while (--k >= 0) {
            spans[j].css(cssName, m[i+2])
            j++
        }
    }
}

function paintRegionBoolean(m, def, spans, cssName, name, invert) {
    if (m == undefined)
        return;

    for (var i = 0; i < m.length; i += 2) {
        var j = m[i]
        var k = m[i+1]
        while (--k >= 0) {
            spans[j].css(cssName, (def[j]^invert) ? name : 'normal' )
            j++
        }
    }
}

function readNum(b) {
    if (b.i >= b.s.length) {
        return null;
    }
    var a = b.s.charCodeAt(b.i)
    var c;
    if (a >= 'a'.charCodeAt(0) && a <= 'z'.charCodeAt(0)) {
        c = 'a'.charCodeAt(0)
    } else if (a >= 'A'.charCodeAt(0) && a <= 'Z'.charCodeAt(0)) {
        c = 'A'.charCodeAt(0)
    } else {
        return null;
    }

    var res = 0;
    do {
        res *= 26;
        res += a - c

        b.i++;
        if (b.i >= b.s.length)
            break;
        a = b.s.charCodeAt(b.i)
    } while ((a-c) >= 0 && (a-c) < 26 );

    return res;
}

function lookChar(b) {
    if (b.i >= b.s.length) {
        return null;
    }
    return b.s.charAt(b.i)
}

function readDup(b, str, c) {
    var a = lookChar(b)
    if (a == null)
        return null;
    if (a == c) {
        b.i++
        return readNum(b) + str.length + 1
    }
    var k = str.indexOf(a)
    if (k == -1)
        return null
    b.i++
    return k+1
}

function createArray(x, len) {
    var res = new Array(len)
    for (var i = 0; i < len; i++) {
        res[i] = x
    }
    return res
}

function unpackColorRegion(b, colors) {
    var res = []
    var i = 0
    do {
        var a = readNum(b)
        if (a != null) {
            var k = readDup(b, colorDupOpt, '*')
            if (k == null) {
                k = 1
            } else {
                k++
            }
            res.push(i)
            res.push(k)
            res.push(colors[a])
        } else {
            k = readDup(b, colorSkipOpt, '-')
            if (k == null)
                break
        }
        i += k
    } while (true)
    return res
}

function unpackColor(s, def, len, colors) {
    if (s == undefined) {
        return createArray(def, len)
    }

    var res = new Array(len)

    var b = {i:0, s:s};
    var i = 0;

    do {
        var x = readNum(b)
        if (x != null) {
            res[i++] = colors[x]
        } else {
            var k = readDup(b, colorSkipOpt, '-')
            if (k != null) {
                while (--k >= 0) {
                    res[i++] = def
                }
            } else {
                k = readDup(b, colorDupOpt, '*')
                if (k != null) {
                    var m = res[i-1]
                    while (--k >= 0) {
                        res[i++] = m
                    }
                } else {
                    break;
                }
            }
        }
    } while (true);

    while (i < len) {
        res[i++] = def
    }

    return res
}

function unpackBooleanRegion(b) {
    var res = []
    var a = lookChar(b)
    var f = !(a >= 'a' && a <= 'z');

    var i = 0
    do {
        a = readNum(b)
        if (a == null)
            break
        a++
        if (f) {
            res.push(i)
            res.push(a)
        }
        i += a
        f = !f
    } while (true)
    return res
}

function unpackBoolean(s, def, len) {
    if (s == undefined || s.length == 0) {
        return createArray(def, len)
    }

    var res = new Array(len)
    var i = 0;

    var b = {i:0, s:s};
    var a = lookChar(b)
    var f = !(a >= 'a' && a <= 'z');

    do {
        var k = readNum(b)
        if (k == null)
            break

        k++
        while (--k >= 0) {
            res[i++] = f
        }

        f = !f
    } while (true)

    while (i < len) {
        res[i++] = def
    }

    return res
}


function getHintPanel() {
    var res = $('#regexHintPanel')
    if (res.length == 0) {
        var outDiv = $('<div style="display:none;position:absolute;background:#eee;padding:2px"/>')
                .appendTo(document.body)
        res = $('<div id="regexHintPanel" style="border:1px solid black;padding:1px;background:#f5fff5;font-size:12px"/>').appendTo(outDiv)
    }
    return res
}