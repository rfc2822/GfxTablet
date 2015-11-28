TARGET = networktablet
PREFIX = /usr/local
BINDIR = $(PREFIX)/bin

networktablet : networktablet.c protocol.h

clean :
	rm -f networktablet

install :
	install -D networktablet $(DESTDIR)$(BINDIR)/networktablet
