TARGET = networktablet
PREFIX = /usr/local
BINDIR = $(PREFIX)/bin

networktablet : 
	g++ -Wall -o "networktablet" "networktablet.cxx"
	
debug :
	g++ -Wall -g -o "networktablet-debug" "networktablet.cxx"

clean :
	rm -f networktablet

install :
	install -D networktablet $(DESTDIR)$(BINDIR)/networktablet
	
uninstall :
	rm -f networktablet networktablet-debug $(DESTDIR)$(BINDIR)/networktablet
	
portable64 :
	g++ -Wall -m64 -o "networktablet64" "networktablet.cxx"
	
portable32 :
	g++ -Wall -m32 -o "networktablet32" "networktablet.cxx"
