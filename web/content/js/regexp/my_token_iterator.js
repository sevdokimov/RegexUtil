define('ess/regex/my_token_iterator', [], function(require, exports, module) {
"use strict";

var MyTokenIterator = function(session, initialRow) {
    this.$session = session;
    this.$row = initialRow;
    this.$rowTokens = session.getTokens(initialRow);
    this.$column = 0
  
  while (this.$rowTokens.length == 0 && this.$row < session.getLength() - 1) {
    this.$row++
    this.$rowTokens = session.getTokens(this.$row);
  }
  
    var token = this.$rowTokens[0];
    this.$tokenIndex = token ? 0 : -1;
};

(function() {
  
    this.stepForward = function() {
        var t = this.$rowTokens[this.$tokenIndex]
        if (t) {
          this.$column += t.value.length
        }
      
        this.$tokenIndex += 1;
      
        var rowCount;
        while (this.$tokenIndex >= this.$rowTokens.length) {
            this.$row += 1;
            if (!rowCount)
                rowCount = this.$session.getLength();
            if (this.$row >= rowCount) {
                this.$row = rowCount - 1;
                return null;
            }

            this.$rowTokens = this.$session.getTokens(this.$row);
            this.$tokenIndex = 0;
            this.$column = 0;
        }
            
        return this.$rowTokens[this.$tokenIndex];
    };
 
    /**
    * 
    * Returns the current tokenized string.
    * @returns {String}
    **/      
    this.getCurrentToken = function () {
        return this.$rowTokens[this.$tokenIndex];
    };

    this.getCurrentTokenRow = function () {
        return this.$row;
    };

    this.getCurrentTokenColumn = function () {
        return this.$column;
    };

    /**
    * 
    * Returns the current column.
    * @returns {Number}
    **/     
    this.getCurrentTokenColumn = function() {
        var rowTokens = this.$rowTokens;
        var tokenIndex = this.$tokenIndex;
        
        // If a column was cached by EditSession.getTokenAt, then use it
        var column = rowTokens[tokenIndex].start;
        if (column !== undefined)
            return column;
            
        column = 0;
        while (tokenIndex > 0) {
            tokenIndex -= 1;
            column += rowTokens[tokenIndex].value.length;
        }
        
        return column;  
    };
            
}).call(MyTokenIterator.prototype);

exports.MyTokenIterator = MyTokenIterator;
});
