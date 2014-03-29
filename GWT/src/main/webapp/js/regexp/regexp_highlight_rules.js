define('ace/mode/regexp_highlight_rules', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text_highlight_rules'],
       function (require, exports, module) {


         var oop = require("../lib/oop");
         var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

         var oneSymbolMap = {
           '(': "openBracket",
           ')': 'closedBracket',
           '.': 'dote',
           '*': 'multiplexer',
           '|': 'orSymbol'
         }

         var oneSymbolRegex = "["
         for (var symbol in oneSymbolMap) {
           if (!oneSymbolMap.hasOwnProperty(symbol)) continue

           assert(symbol.match(/[^a-zA-Z0-9]/))
           oneSymbolRegex += '\\' + symbol
         }
         oneSymbolRegex += ']'

         var RegexpFileHighlightRules = function () {

           this.$rules = {
             "#atom": [
               { token: "dote",
                 regex: /\./,
                 next: "afterAtom"
               },

               { token: 'controlLetter',
                 regex: /\\c[a-z]/i,
                 next: "afterAtom"
               },
               { token: 'characterClassEscape',
                 regex: /\\[sSdDwW]/,
                 next: "afterAtom"
               },
               { token: 'controlEscape',
                 regex: /\\[fnrtv]/,
                 next: "afterAtom"
               },

               { token: "defText",
                 regex: /[^\^\$\\\.\|\*\+\?\(\)\[\]\{\}\/]/,
                 next: "afterAtom"
               }
             ],

             "#term": [
               { include: "#atom" },

               { token: "assertion",
                 regex: /\^|\$|\\b|\\B/,
                 next: "start"
               },

               { token: 'orSymbol',
                 regex: /\|/,
                 next: "start"
               },

               { token: 'openBracket',
                 regex: /\((?:\?[:=!])?/,
                 next: "start"
               },

               { token: 'closedBracket',
                 regex: /\)/,
                 next: "afterAtom"
               }
             ],

             start: [
               { include: "#term" },

               { token: 'error',
                 regex: /[*+?]|\{\d+(?:,\d*)?\}\??/,
                 next: "start"
               },

               {
                 token: 'error',
                 regex: /./
               }
             ],

             afterAtom: [
               { include: "#term" },

               { token: 'quantifier.ace_specSymbol',
                 regex: /[*+?]|\{\d+(?:,\d*)?\}\??/,
                 next: "start"
               },

               {
                 token: 'error',
                 regex: /./
               }
             ]
           }

           this.normalizeRules();
         };

         oop.inherits(RegexpFileHighlightRules, TextHighlightRules);

         exports.RegexpFileHighlightRules = RegexpFileHighlightRules;
       });
