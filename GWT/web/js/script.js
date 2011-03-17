function attachPopup(e, popup) {
    var popupDiv = $('<div style="display:none;position:absolute;background:#ccc;padding:5px"/>').appendTo(document.body)

    $(popup).css('border', '1px solid black').appendTo(popupDiv);

    var onmove = function(event) {
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
    e.onmousemove = onmove;

    e.onmouseover = function(event){
        if (!event) event = window.event;
        onmove(event);
        popupDiv.css('display', 'block')
    }

    e.onmouseout = function () {
        popupDiv.css('display', 'none')
    }
}

function mapToString() {
    
}