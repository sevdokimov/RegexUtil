var rIpAddr = {
regex:'\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b',
h:['\\b - A word boundary'
,0,'Non-capturing group'
,2,2,2,2,2,'\'2\' (\\u0032)'
,'\'5\' (\\u0035)'
,'Character set<br><br>Matches a single character out of the set.<br><br>Example:<br>The expression "[ecl]" matches "c" and "l" in text "cold".<br>The expression "[a-z&amp;&amp;[^ecl]]" matches any character from a to z, excluding e, c, and l.'
,'Matches all character from \'0\' (\\u0030) to \'5\' (\\u0035)'
,11,11,''
,'U|V - Alternation: U or V<br><br>First tries to match subexpression U. Falls back and tries to match V if U didn\'t match.<br><br>Examples:<br>- The expression "A|B" applied to text "BA" first matches "B", then "A".<br>- The expression "AB|BC|CD" applied to text "ABC BC DAB" matches, in sequence:<br>  "AB" in the first word, the second word "BC", "AB" at the very end.'
,8,10,'Matches all character from \'0\' (\\u0030) to \'4\' (\\u0034)'
,18,18,14,10,'Matches all character from \'0\' (\\u0030) to \'9\' (\\u0039)'
,23,23,14,15,10,'\'0\' (\\u0030)'
,'\'1\' (\\u0031)'
,14,'? - Greedy match 0 or 1 times'
,10,23,23,23,14,10,23,23,23,14,32,14,'\'.\' (\\u002E)'
,45,14,'{n} - Greedy match exactly n times'
,48,48,2,2,2,8,9,10,11,11,11,14,15,8,10,18,18,18,14,10,23,23,23,14,15,10,29,30,14,32,10,23,23,23,14,10,23,23,23,14,32,14,0,0],
df:0,db:1,dw:false,di:false,
f:'c0(d1@d1~d1&d1~d1^e^d1@d1~d1&d1~d1!c0',
w:'bFbAcBaAcBcCbCcBcCbBaDbAcBaAcBcCbCcBcC',
s:'f0:~g1-bbG<#g1-vG<:)g!g;_f1<)g!g;&h5`h*q;;-cG!g;-dF1<-cG!g;-hG!g;-iF1<-hG!g;&h*h`h*e;-nG~g;:-nG~g;-nH2;-sG!g;-tF1<-sG!g;-xG!g;-yF1<-xG!g;-xH3;#g1-vG;-beF0:~g1-bbG;~h*biF1<-bkG1-vG<:-bpG!g;-bqF1<-bpG!g;-bnH5`h*q;;-bwG!g;-bxF1<-bwG!g;-cbG!g;-ccF1<-cbG!g;-bnH*h`h*e;-chG~g;:-chG~g;-chH2;-cmG!g;-cnF1<-cmG!g;-crG!g;-csF1<-crG!g;-crH3;-bkG1-vG;-cyF0:',
c:'000000ffffff7878000096000000ffebebeb8d9efcc8e7f2'}

var rMacAddr = {
regex:'^([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])$',
h:['\'^\' Begin of input sequence.<br>To make the \'^\' match line start (after a line terminator) as well,switch on flag "Multiline" (?m)'
,'Capturing group #1'
,'Character set<br><br>Matches a single character out of the set.<br><br>Example:<br>The expression "[ecl]" matches "c" and "l" in text "cold".<br>The expression "[a-z&amp;&amp;[^ecl]]" matches any character from a to z, excluding e, c, and l.'
,'Matches all character from \'0\' (\\u0030) to \'9\' (\\u0039)'
,3,3,'Matches all character from \'a\' (\\u0061) to \'f\' (\\u0066)'
,6,6,'Matches all character from \'A\' (\\u0041) to \'F\' (\\u0046)'
,9,9,''
,2,3,3,3,6,6,6,9,9,9,12,'\':\' (\\u003A)'
,12,'{n} - Greedy match exactly n times'
,26,26,'Capturing group #2'
,2,3,3,3,6,6,6,9,9,9,12,2,3,3,3,6,6,6,9,9,9,12,12,'\'$\' End of input sequence.<br>To make the \'$\' match line end (before a line terminator) as well,switch on flag "Multiline" (?m)'
],
df:0,db:1,dw:false,di:false,
f:'c~d7~d7@e!d7~d7~c',
w:'aBiBiAaBaCiBiB',
s:';`f-iF;~f(f;!g1<%g1<(g1<~f(f;|f(f;{g1<-cG1<-fG1<|f(f;;`f-iF;`h*nG1<-oF-hF;-pF(f;-qG1<-tG1<-wG1<-pF(f;-baF(f;-bbG1<-beG1<-bhG1<-baF(f;-oF-hF;;',
c:'000000ffffff7878000096000000ff8d9efcebebebc8e7f2'}

var rDomainName = {
regex:'^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}$',
h:['\'^\' Begin of input sequence.<br>To make the \'^\' match line start (after a line terminator) as well,switch on flag "Multiline" (?m)'
,'Capturing group #1'
,'Character set<br><br>Matches a single character out of the set.<br><br>Example:<br>The expression "[ecl]" matches "c" and "l" in text "cold".<br>The expression "[a-z&amp;&amp;[^ecl]]" matches any character from a to z, excluding e, c, and l.'
,'Matches all character from \'a\' (\\u0061) to \'z\' (\\u007A)'
,3,3,'Matches all character from \'A\' (\\u0041) to \'Z\' (\\u005A)'
,6,6,'Matches all character from \'0\' (\\u0030) to \'9\' (\\u0039)'
,9,9,''
,'Capturing group #2'
,2,3,3,3,6,6,6,9,9,9,'\'-\' (\\u002D)'
,24,12,'{n,m} - Greedy match &gt;= n times but &lt;= m times'
,27,27,27,27,27,2,3,3,3,6,6,6,9,9,9,12,12,'? - Greedy match 0 or 1 times'
,'\'.\' (\\u002E)'
,46,12,'+ - Greedy match 1 or more times'
,2,3,3,3,6,6,6,12,27,27,27,27,27,'\'$\' End of input sequence.<br>To make the \'$\' match line end (before a line terminator) as well,switch on flag "Multiline" (?m)'
],
df:0,db:1,dw:false,di:false,
f:'c~d7!d7@e`e0~d7&d4~e`e`c',
w:'aBiCkBaAbBiCbCfBaAaA',
s:';`f-bfF;~f(f;!g1<%g1<(g1<~f(f;|f-pF;{f_f;-aG1<-dG1<-gG1<-jG0:{f_f;{h*bG4.-sF(f;-tG1<-wG1<-zG1<-sF(f;|f-pF;|h*u;-bfG0:`f-bfF;`h*bk;-bjF%f;-bkG1<-bnG1<-bjF%f;-bjH6g3,;',
c:'000000ffffff7878000096000000ff8d9efcebebebc8e7f2'}

var rFileName = {
regex:'(?i)^(?!^(PRN|AUX|CLOCK\\$|NUL|CON|COM\\d|LPT\\d|\\..*)(\\..+)?$)[^\\\\\\./:\\*\\?\\"<>\\|][^\\\\/:\\*\\?\\"<>\\|]{0,254}$',
h:['Flag modification'
,0,0,''
,'\'^\' Begin of input sequence.<br>To make the \'^\' match line start (after a line terminator) as well,switch on flag "Multiline" (?m)'
,'Non-capturing group'
,5,5,4,'Capturing group #1'
,'\'P\' (\\u0050)'
,'\'R\' (\\u0052)'
,'\'N\' (\\u004E)'
,'U|V - Alternation: U or V<br><br>First tries to match subexpression U. Falls back and tries to match V if U didn\'t match.<br><br>Examples:<br>- The expression "A|B" applied to text "BA" first matches "B", then "A".<br>- The expression "AB|BC|CD" applied to text "ABC BC DAB" matches, in sequence:<br>  "AB" in the first word, the second word "BC", "AB" at the very end.'
,'\'A\' (\\u0041)'
,'\'U\' (\\u0055)'
,'\'X\' (\\u0058)'
,13,'\'C\' (\\u0043)'
,'\'L\' (\\u004C)'
,'\'O\' (\\u004F)'
,18,'\'K\' (\\u004B)'
,'\'$\' (\\u0024)'
,23,13,12,15,19,13,18,20,12,13,18,20,'\'M\' (\\u004D)'
,'\\d - A digit [0-9]'
,37,13,19,10,'\'T\' (\\u0054)'
,37,37,13,'\'.\' (\\u002E)'
,46,'The dot matches any character except line terminators.<br><br>To make the dot match line terminators as well,<br>switch on flag "Dotall" (?s)<br>'
,'* - Greedy match 0 or more times'
,3,'Capturing group #2'
,46,46,48,'+ - Greedy match 1 or more times'
,3,'? - Greedy match 0 or 1 times'
,'\'$\' End of input sequence.<br>To make the \'$\' match line end (before a line terminator) as well,switch on flag "Multiline" (?m)'
,3,'Excluded character set<br><br>Matches a single character that is not one of the excluded characters.<br><br>Examples:<br>The expression "[^ecl]" matches "o" and "d" in text "cold".<br>The expression "[a-z&amp;&amp;[^ecl]]" matches any character from a to z, excluding e, c, and l.'
,60,'\'\\\' (\\u005C)'
,62,'\'.\' (\\u002E) (escaping is not necessarily)'
,64,'\'&#47;\' (\\u002F)'
,'\':\' (\\u003A)'
,'\'*\' (\\u002A) (escaping is not necessarily)'
,68,'\'?\' (\\u003F) (escaping is not necessarily)'
,70,'\'"\' (\\u0022) (escaping is not necessarily)'
,72,'\'&lt;\' (\\u003C)'
,'\'&gt;\' (\\u003E)'
,'\'|\' (\\u007C) (escaping is not necessarily)'
,76,3,60,60,62,62,66,67,68,68,70,70,72,72,74,75,76,76,3,'{n,m} - Greedy match &gt;= n times but &lt;= m times'
,96,96,96,96,96,96,58],
df:0,db:1,dw:false,di:false,
f:'@c!c-nD0@d0|c#e0~e4~e0^e4~e0~f`f1`c',
w:'DaCaAcAcAgAcAcAeAeAbDbDaCpCnBaAcA',
s:'g2>;#g1-bkG<;(g-zG;<)h1`h*y;<)h5`h*u;,-iI0:)h*d`h*m;<)h*h`h*i;<)h*l`h*e;<-wI0:)h*r`h8;<-bcI0:)h*x`h2;-bfI0:;-bhH;(g-zG;-bkG@g;-blI0:;-bnH;-bkG@g;-bkH4;;#g1-bkG;-btG0-bG:-bvI0:-bxI0::-cbI0:-cdI0:-cfI0::-cjI0:-btG0-bG;-cmG0{g:-coI0::-csI0:-cuI0:-cwI0::-daI0:-cmG0{g;-cmH*fI5?;',
c:'000000ffffff7878000096006464640000ff8d9efcc8e7f2ebebeb'}

var rFloat = {
regex:'[-+]?(?:\\b[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+\\b)(?:[eE][-+]?[0-9]+\\b)?',
h:['Character set<br><br>Matches a single character out of the set.<br><br>Example:<br>The expression "[ecl]" matches "c" and "l" in text "cold".<br>The expression "[a-z&amp;&amp;[^ecl]]" matches any character from a to z, excluding e, c, and l.'
,'\'-\' (\\u002D)'
,'\'+\' (\\u002B)'
,''
,'? - Greedy match 0 or 1 times'
,'Non-capturing group'
,5,5,'\\b - A word boundary'
,8,0,'Matches all character from \'0\' (\\u0030) to \'9\' (\\u0039)'
,11,11,3,'+ - Greedy match 1 or more times'
,5,5,5,'\'.\' (\\u002E)'
,19,0,11,11,11,3,'* - Greedy match 0 or more times'
,3,4,'U|V - Alternation: U or V<br><br>First tries to match subexpression U. Falls back and tries to match V if U didn\'t match.<br><br>Examples:<br>- The expression "A|B" applied to text "BA" first matches "B", then "A".<br>- The expression "AB|BC|CD" applied to text "ABC BC DAB" matches, in sequence:<br>  "AB" in the first word, the second word "BC", "AB" at the very end.'
,19,19,0,11,11,11,3,15,8,8,3,5,5,5,0,'\'e\' (\\u0065)'
,'\'E\' (\\u0045)'
,3,0,1,2,3,4,0,11,11,11,3,15,8,8,3,4],
df:0,db:1,dw:false,di:false,
f:'&c0`d1&d1&d1~c0{d1~c0',
w:'AbEbAcEbAcEbAcBbEbBbCcBbB',
s:'e~e;:e~e;f2;#e1-rE<&g0:)e!e;_g1<)e!e;)f3;-bE1&e<-eG0:-gE!e;-hG1<-gE!e;-gF3;-bE1&e;-bF*a;&f*j`f8;-pG0:-rE!e;-sG1<-rE!e;-rF3;-xG0:#e1-rE;-baE1-cE<-bdE~e;:-bdE~e;-bhE~e;:-bhE~e;-bhF2;-bmE!e;-bnG1<-bmE!e;-bmF3;-bsG0:-baE1-cE;-baF*j;',
c:'000000ffffff7878000096008d9efcc8e7f2ebebeb'}

var rRomanNumber = {
regex:'^(?i:(?=[MDCLXVI])((M{0,3})((C[DM])|(D?C{0,3}))?((X[LC])|(L?XX{0,2})|L)?((I[VX])|(V?(II{0,2}))|V)?))$',
h:['\'^\' Begin of input sequence.<br>To make the \'^\' match line start (after a line terminator) as well,switch on flag "Multiline" (?m)'
,'Non-capturing group'
,1,1,1,1,1,1,'Character set<br><br>Matches a single character out of the set.<br><br>Example:<br>The expression "[ecl]" matches "c" and "l" in text "cold".<br>The expression "[a-z&amp;&amp;[^ecl]]" matches any character from a to z, excluding e, c, and l.'
,'\'M\' (\\u004D)'
,'\'D\' (\\u0044)'
,'\'C\' (\\u0043)'
,'\'L\' (\\u004C)'
,'\'X\' (\\u0058)'
,'\'V\' (\\u0056)'
,'\'I\' (\\u0049)'
,''
,16,'Capturing group #1'
,'Capturing group #2'
,9,'{n,m} - Greedy match &gt;= n times but &lt;= m times'
,21,21,21,21,16,'Capturing group #3'
,'Capturing group #4'
,11,8,10,9,16,16,'U|V - Alternation: U or V<br><br>First tries to match subexpression U. Falls back and tries to match V if U didn\'t match.<br><br>Examples:<br>- The expression "A|B" applied to text "BA" first matches "B", then "A".<br>- The expression "AB|BC|CD" applied to text "ABC BC DAB" matches, in sequence:<br>  "AB" in the first word, the second word "BC", "AB" at the very end.'
,'Capturing group #5'
,10,'? - Greedy match 0 or 1 times'
,11,21,21,21,21,21,16,16,38,'Capturing group #6'
,'Capturing group #7'
,13,8,12,11,16,16,35,'Capturing group #8'
,12,38,13,13,21,21,21,21,21,16,35,12,16,38,'Capturing group #9'
,'Capturing group #10'
,15,8,14,13,16,16,35,'Capturing group #11'
,14,38,'Capturing group #12'
,15,15,21,21,21,21,21,16,16,35,14,16,38,16,16,'\'$\' End of input sequence.<br>To make the \'$\' match line end (before a line terminator) as well,switch on flag "Multiline" (?m)'
],
df:0,db:1,dw:false,di:false,
f:'c-gD`d-bD`d-eD`d-hD`d(c',
w:'aHgDaAaAaDaAbDaAaAaAaFaAbDaAbAaAaCaDaAbDaBbAaAaDaD',
s:';`e2-dbE>#e1(e<&e^e;?&e^e;#e1(e;-dE-cmE;-eE%e;;-fFg3,-eE%e;-mE-dE;-nE#e;;-pE~e;:-pE~e;-nE#e;-nF5`f8;-vE&e;;-wF;;-yFg3,-vE&e;-mE-dE;-mF*i;-bhE-gE;-biE#e;;-bkE~e;:-bkE~e;-biE#e;-biF5`f*b;-bqE(e;;-brF;:-buFg3,-bqE(e;-biF*h`f;;-bhE-gE;-bhF*l;-cfE-iE;-cgE#e;;-ciE~e;:-ciE~e;-cgE#e;-cgF5`f*d;-coE_e;;-cpF;-crE^e;:-ctFg3,-crE^e;-coE_e;-cgF*j`f;;-cfE-iE;-cfF*n;-dE-cmE;`e2-dbE;;',
c:'000000ffffff7878000000ff8d9efcc8e7f2ebebeb'}

var rDate = {
regex:'(19|20)\\d\\d([- /.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])',
h:['Capturing group #1'
,'\'1\' (\\u0031)'
,'\'9\' (\\u0039)'
,'U|V - Alternation: U or V<br><br>First tries to match subexpression U. Falls back and tries to match V if U didn\'t match.<br><br>Examples:<br>- The expression "A|B" applied to text "BA" first matches "B", then "A".<br>- The expression "AB|BC|CD" applied to text "ABC BC DAB" matches, in sequence:<br>  "AB" in the first word, the second word "BC", "AB" at the very end.'
,'\'2\' (\\u0032)'
,'\'0\' (\\u0030)'
,''
,'\\d - A digit [0-9]'
,7,7,7,'Capturing group #2'
,'Character set<br><br>Matches a single character out of the set.<br><br>Example:<br>The expression "[ecl]" matches "c" and "l" in text "cold".<br>The expression "[a-z&amp;&amp;[^ecl]]" matches any character from a to z, excluding e, c, and l.'
,'\'-\' (\\u002D)'
,'\' \' (\\u0020)'
,'\'&#47;\' (\\u002F)'
,'\'.\' (\\u002E)'
,6,6,'Capturing group #3'
,5,12,'Matches all character from \'1\' (\\u0031) to \'9\' (\\u0039)'
,22,22,6,3,1,12,5,1,4,6,6,'\\i - Match of the capturing group i'
,34,'Capturing group #4'
,5,12,22,22,22,6,3,12,1,4,6,12,'Matches all character from \'0\' (\\u0030) to \'9\' (\\u0039)'
,49,49,6,3,'\'3\' (\\u0033)'
,12,5,1,6,6],
df:0,db:1,dw:false,di:false,
f:'^c2_c1(d0!c1^c1',
w:'AbAbAdBdCaAcBaAcBbAaAcCbBcBaAbB',
s:'e#e;:`f0`f0;:e#e;^g0:(g0:_e%e;=e@e;>=e@e;_e%e;-eE|e;;-gE!e;-hG1<-gE!e;-fF4`f4;;-nE!e;<-nE!e;-eE|e;_f6-aG0:-vE-hE;;-xE!e;-yG1<-xE!e;-wF4`f*d;-bdE~e;:-bdE~e;-bhE!e;-biG1<-bhE!e;-wF*e`f3;;-boE~e;:-boE~e;-vE-hE;',
c:'000000ffffff0096000000ff8d9efcc8e7f2ebebeb'}