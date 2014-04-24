define('ace/mode/regexp_highlight_rules', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text_highlight_rules'],
       function (require, exports, module) {


         var oop = require("../lib/oop");
         var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

         var RegexpFileHighlightRules = function () {

           this.$rules = {
             "#atom": [
               { token: "dote",
                 regex: /\./,
                 next: "afterAtom",
                 merge: false
               },

               { token: ['numEsc', 'controlLetter', 'controlEsc'],
                 regex: /(\\(?:0[1-9][0-9]*|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}))|(\\c[a-zA-Z])|(\\[fnrtv])/,
                 next: "afterAtom",
                 merge: false
               },
               { token: 'charClassEsc',
                 regex: /\\[sSdDwW]/,
                 next: "afterAtom",
                 merge: false
               },
               { token: ['escapeSymbol', 'escapedSymbol'],
                 regex: /(\\)([^a-zA-Z0-9])/,
                 next: "afterAtom",
                 merge: false
               },

               { token: "defText",
                 regex: /[^\^\$\\\.\|\*\+\?\(\)\[\/]/,
                 next: "afterAtom"
               }
             ],

             "#term": [
               { include: "#atom" },

               { token: "assertion",
                 regex: /\^|\$|\\b|\\B/,
                 next: "start",
                 merge: false
               },

               { token: 'orSymbol',
                 regex: /\|/,
                 next: "start",
                 merge: false
               },

               { token: 'openBracket',
                 regex: /\((?:\?[:=!])?/,
                 next: "start",
                 merge: false
               },

               { token: 'closedBracket',
                 regex: /\)/,
                 next: "afterAtom",
                 merge: false
               },
                 
               { token: "error",
                 regex: /\[\^?\]/,
                 next: "afterAtom"
               },
                 
               { token: "charClassStart",
                 regex: /\[\^?/,
                 next: "charClassStart"
               }
             ],

             start: [
               { include: "#term" },

               { token: 'error',
                 regex: /[*+?]|\{\d+(?:,\d*)?\}\??/,
                 next: "start"
               },

               {
                 token: 'error.incorrectEsc',
                 regex: /\\./
               },
                 
               {
                 token: 'error',
                 regex: /./
               }
             ],

             afterAtom: [
               { token: 'quantifier',
                 regex: /(?:[*+?]|\{\d+(?:,\d*)?\})\??/,
                 next: "start",
                 merge: false
               },

               { include: "#term" },
                 
               {
                 token: 'error.incorrectEsc',
                 regex: /\\./
               },

               {
                 token: 'error',
                 regex: /./
               }
             ],

             "#charClassAtom": [
               { token: 'charClassEsc',
                 regex: /\\[sSdDwW]/,
                 next: "charClassStart",
                 merge: false
               },

               { token: ['charClassAtom', 'numEsc', 'controlLetter', 'controlEsc', 'escapeSymbol', 'escapedSymbol'],
                 regex: /([^\]\\])|(\\(?:[0-9]+|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}))|(\\c[a-zA-Z])|(\\[fnrtv])|(\\)(.)/,
                 next: "charClassAfterAtom",
                 merge: false
               },

               { token: 'charClassEnd',
                 regex: /]/,
                 next: "afterAtom",
                 merge: false
               }
             ]  ,
             
             charClassStart: [
               {
                 include: "#charClassAtom"
               }
             ],
             
             charClassAfterAtom: [
               { token: ['charClassRange', 'charClassAtom', 'numEsc', 'controlLetter', 'controlEsc', 'escapeSymbol', 'escapedSymbol'],
                 regex: /(-)(?:([^\]\\])|(\\(?:[0-9]+|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}))|(\\c[a-zA-Z])|(\\[fnrtv])|(\\)([^sSdDwW]))/,
                 next: "charClassStart",
                 merge: false
               },

               {
                 include: "#charClassAtom"
               }
             ]
           }

           this.normalizeRules();
         };

         oop.inherits(RegexpFileHighlightRules, TextHighlightRules);

         exports.RegexpFileHighlightRules = RegexpFileHighlightRules;
       });
