SHELL:=/bin/sh
PAPER:=paper
BIBTEX:=cites
.PHONY: default clean
.DEFAULT: default

default:
	pdflatex ${PAPER}.tex
	bibtex ${PAPER}.aux
	pdflatex ${PAPER}.tex
	pdflatex ${PAPER}.tex 

clean:
	rm -f *.pdf *.aux *.bbl *.blg *.dvi *.log *.out

