%{

#include <stdio.h>
#include "y.tab.h"

%}

/* Definitions */

/*
delim	[ \t\n]+
if	"if"
then	"then"
else	"else"
begin	"begin"
end	"end"
while	"while"
do	"do"
prgm	"program"
var	"var"
as	"as"
type	"int"
proc1	"writeInt"
proc2	"readInt"
idk	"(, )"
asgn	":="
semi	";"
add	"+"
sub	"-"
mpy	"*"
div	"div"|"/"
mod	"mod"
eq	"="
neq	"!="
lt	"<"
lteq	"<="
gt	">"
gteq	">="
lp	"("
rp	")"
com	"%"
id	[A-Z][A-Z0-9]*
num	(+|-)?[1-9][0-9]*|0

*/

/* Rules */

%%

[ \t\n]+			{;}
"%".*				{;}
"if"				return IF;
"then"				return THEN;
"else"				return ELSE;
"begin"				return START;
"end"				return END;
"while"				return WHILE;
"do"				return DO;
"program"			return PROGRAM;
"var"				return VAR;
"as"				return AS;
"int"				return INT;
"writeInt"			return WRITEINT;
"readInt"			return READINT;
":="				return ASGN;
";"				return SC;
"+"				return ADD;
"-"				return SUB;
"*"				return MULT;
"div"				return DIV;
"mod"				return MOD;
"="				return EQ;
"!="				return NEQ;
"<"				return LT;
"<="				return LTEQ;
">"				return GT;
">="				return GTEQ;
"("				return LP;
")"				return RP;
[A-Z][A-Z0-9]*			{yylval.sym = strdup(yytext); return ID;}
("+"|"-")?[1-9][0-9]*|0		{yylval.num = atoi(yytext); return NUM;}
.				{printf("ERROR: Token not recognized\n"); exit(1);}

%%

/* C code */

int yywrap() {
	return 1;
}
