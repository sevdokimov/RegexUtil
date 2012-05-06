var epExampleRegex = {
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


