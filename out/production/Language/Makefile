all: environment.class evaluator.class lexeme.class lexer.class main.class parser.class recognizer.class scanner.class

environment.class: environment.java
	javac -d . -classpath . environment.java

evaluator.class: evaluator.java
	javac -d . -classpath . evaluator.java

lexeme.class: Lexeme.java
	javac -d . -classpath . Lexeme.java

lexer.class: Lexer.java
	javac -d . -classpath . Lexer.java

main.class: main.java
	javac -d . -classpath . main.java

recognizer.class: recognizer.java
	javac -d . -classpath . recognizer.java

parser.class: parser.java
	javac -d . -classpath . parser.java

scanner.class: scanner.java
	javac -d . -classpath . scanner.java

clean:
	rm -f *.class

run-arrays:
	java main 
