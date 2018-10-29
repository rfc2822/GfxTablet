TARGET = networktablet
PREFIX = /usr/local
BINDIR = $(PREFIX)/bin

networktablet: networktablet.c
	gcc networktablet.c -o networktablet -lX11 `pkg-config --cflags --libs gtk+-3.0`

clean :
	rm -f networktablet

install :
	install -D networktablet $(DESTDIR)$(BINDIR)/networktablet
