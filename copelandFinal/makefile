CC     = gcc
YACC   = yacc
YFLAGS = -dv
LEX    = lex
	
vsl      : y.tab.c lex.yy.c
	$(CC) y.tab.c lex.yy.c -o vsl
y.tab.c  :
	$(YACC) $(YFLAGS) vslgram.y
lex.yy.c : 
	$(LEX) vslLex.l

