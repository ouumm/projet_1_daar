all:
	mkdir -p bin
	javac -d bin src/daar/*.java
	printf '#!/bin/sh\nDIR=$$(dirname "$$0")\nexec java -cp "$$DIR/bin" daar.Egrep "$$@"\n' > egrep-clone
	chmod +x egrep-clone

test: all
	sh tests/run_tests.sh

bench: all
	sh tests/bench.sh

clean:
	rm -rf bin egrep-clone
