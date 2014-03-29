ace.define('ace/mode/regexp',
           ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text', 'ace/tokenizer', 'ace/mode/regexp_highlight_rules'],
           function (require, exports, module) {

             var oop = require("../lib/oop");
             var TextMode = require("./text").Mode;
             var Tokenizer = require("../tokenizer").Tokenizer;
             var RegexpFileHighlightRules = require("./regexp_highlight_rules").RegexpFileHighlightRules;

             var Mode = function () {
               this.HighlightRules = RegexpFileHighlightRules;
             };
             oop.inherits(Mode, TextMode);

             (function () {
               this.lineCommentStart = "";
               this.blockComment = "";
               this.$id = "ace/mode/regexp";
             }).call(Mode.prototype);

             exports.Mode = Mode;
           });
