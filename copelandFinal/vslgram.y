%{

#include <stdlib.h>
#include <ctype.h>
#include <stdio.h>
#include <string.h>
#include <stdarg.h>

char *genLabel(void);
void addToSymTable(char*, int);
void error(char*);
int lookup(char*);
//void modVal(char*, int);

//Globals
char filename[256];
int  symKnt;
FILE *infile;
FILE *outfile;

//Variables from lex
extern int yylex();
extern int yylineno;
extern char *yytext;
extern FILE *yyin;

struct sym_record {
	char token[256];
	int  value;
};

struct sym_record table[256];


%}

%union {int num; char *sym;}

%token <sym> LP		
%token <sym> RP		
%token <sym> ASGN		
%token <sym> SC		
%token <sym> MULT		
%token <sym> DIV		
%token <sym> MOD		
%token <sym> ADD		
%token <sym> SUB		
%token <sym> EQ		
%token <sym> NEQ		
%token <sym> LT		
%token <sym> GT		
%token <sym> LTEQ		
%token <sym> GTEQ		
%token <sym> IF		
%token <sym> THEN		
%token <sym> ELSE		
%token <sym> START		
%token <sym> END		
%token <sym> WHILE		
%token <sym> DO		
%token <sym> PROGRAM		
%token <sym> VAR		
%token <sym> AS		
%token <sym> INT		
%token <sym> WRITEINT	
%token <sym> READINT		
%token <sym> ID		
%token <num> NUM

%type <sym>  ifStatement
%type <sym>  whileStatement

%left	MULT
%left	DIV
%left	MOD
%left	ADD
%left	EQ
%left	NEQ
%left	LT
%left	GT
%left	LTEQ
%left	GTEQ		

%%

program           : PROGRAM {fprintf(outfile, "Section\t.data\n");} declarations START {fprintf(outfile, "Section\t.code\n");} statementSequence END
		  ;

declarations      : 

		    | VAR ID AS type SC {addToSymTable($2, 0); fprintf(outfile, "\t%s:\tword\n", $2);} declarations

		  ;

type              : INT
		  ;

statementSequence :
			| statement SC statementSequence /*{;}*/
		  ;

statement         : assignment /*{;}*/ | ifStatement /*{;}*/ | whileStatement /*{;}*/ | writeInt /*{;}*/ 
		  ;

assignment        : ID ASGN {if (lookup($1) == 0) {error("variable undeclared");}} {fprintf(outfile, "\tLVALUE\t%s\n", $1);} expression {fprintf(outfile, "\t:=\n");} | ID ASGN READINT {if (lookup($1) == 0) {error("variable undeclared");} fprintf(outfile, "\tLVALUE\t%s\n\tREAD\n\t:=\n", $1);}
		  ;

ifStatement       : IF expression {$$ = strdup(genLabel());} {fprintf(outfile, "\tGOFALSE\t%s\n", $3);} THEN statementSequence {$$ = strdup(genLabel());} {fprintf(outfile, "\tGOTO\t%s\n", $7);} {fprintf(outfile, "\tLABEL\t%s\n", $3);} elseClause {fprintf(outfile, "\tLABEL\t%s\n", $7);} END 
		  ;

elseClause        : 	
			| ELSE statementSequence /*{;}*/
		  ;

whileStatement    : WHILE {$$ = strdup(genLabel());} {fprintf(outfile, "\tLABEL\t%s\n", $2);} expression {$$ = strdup(genLabel());} {fprintf(outfile, "\tGOFALSE\t%s\n", $5);} DO statementSequence {fprintf(outfile, "\tGOTO\t%s\n", $2);} END {fprintf(outfile, "\tLABEL\t%s\n", $5);}
		  ;

writeInt          : WRITEINT expression {fprintf(outfile, "\tPRINT\n");}
		  ;

expression        : simpleExpression /*{;}*/	                  		     |
		    simpleExpression EQ   expression  {fprintf(outfile, "\tEQ\n");}  |
		    simpleExpression NEQ  expression  {fprintf(outfile, "\tNE\n");}  |
		    simpleExpression LT   expression  {fprintf(outfile, "\tLT\n");}  |
		    simpleExpression GT   expression  {fprintf(outfile, "\tGT\n");}  |
		    simpleExpression LTEQ  expression {fprintf(outfile, "\tLE\n");}  |
		    simpleExpression GTEQ  expression {fprintf(outfile, "\tGE\n");}
		  ;

simpleExpression  : term ADD simpleExpression {fprintf(outfile, "\tADD\n");}|
		    term SUB simpleExpression {fprintf(outfile, "\tSUB\n");}       | 
		    term /*{;}*/
		  ;

term              : factor MULT term  {fprintf(outfile, "\tMPY\n");} |
		    factor DIV  term  {fprintf(outfile, "\tDIV\n");} |
		    factor MOD  term  {fprintf(outfile, "\tMOD\n");} |
		    factor /*{;}*/
		  ;

factor            : ID  {if (lookup($1) == 0) {error("variable undeclared");} fprintf(outfile, "\tRVALUE\t%s\n", $1);}  |
		    NUM {fprintf(outfile, "\tPUSH\t%d\n", $1);} |
		    LP expression RP /*{;}*/
		  ;

%%

int main(int argc, char *argv[]) {

	//Get name of file
	if(argc > 0 && argv[1] != NULL) {
		strcpy(filename, argv[1]);
	} else {
		printf("Enter name of input file: ");
		scanf("%s", filename);
	}

	//Open input file
	infile = fopen(filename, "r");
	
	if(infile == NULL) {
		//error("file not found.");
	}

	yyin = infile;

	//Open output file
	outfile = fopen("out.asm", "w");

	//Initialize counters
	symKnt        = 0;

	//Parse
	do {
		yyparse();
	} while (!feof(yyin));

}


char *genLabel() {
    static int i = 1000;
    char *temp = malloc(5);
    sprintf(temp,"%04d",i++);
    return temp;
}

void addToSymTable(char *lex, int val) {
	char errmsg[20] = " already declared.";
	strcat(errmsg, lex);
	
	//If not already in symbol table
	if(lookup(lex) == 0) {
		strcpy(table[symKnt].token, lex);
		table[symKnt].value = val;

		symKnt++;	
	} else {
		error(errmsg);
	}
}

int lookup(char *lex) {	
	int i = 0;
	int exists;

	//Until end of symbol table
	while(strcmp(table[i].token, "")) {
		exists = strcmp(table[i].token, lex);

		//If it's already in the symbol table, go back
		if(exists == 0) {
			return 1;
		}

		i++;
	}

	//Not in symbol table
	return 0;
}

void modVal(char *lex, int val) {
	int i = 0;

	while(strcmp(table[i].token, lex)) {
		i++;
	}

	table[i].value = val;
}

void error(char *errstr) {
	printf("ERROR: %s\n", errstr);
	exit(1);
}


int yyerror(char *s)
{
    printf("%s\n", s);
}
