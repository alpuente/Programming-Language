Amy Puente
CS 403 Designer Programming Language

This is my implementation of a general purpose programming language for CS403.
The language is written in java. 

To run programs in this language, it is not necessary to have a main function, but proper syntax is required for a functional program. To terminate a program in this language, use the character '~'

To run a program in this language:
	- compile the files using javac
	- use the command "java main filename" where filename denotes a file containing a program following the syntax of this language

Keywords:
- for
- var
- if
- elif
- else
- while
- def
- return
- or
- and
- print
- lambda
- true
- false
- null
- arr

Primitive data types:
integers, doubles, strings ("string"), and booleans (true and false)

Binary operators:
addition (+), subtraction (-), division (/), multiplication (*)

General syntax rules:
- statements must be terminated with a semicolon

- returns take the form of "return primary"

- comments are begun with the characters :) and terminated with (:

- declaring variables:
	- to declare a variable, use the keyword "var" followed by an alphanumeric variable name that isn't a designated keyword, followed by "=", a primary and a semicolon
	- example: var x = "hello";

- defining functions:
	- to define a function, use the keyword "def" followed by a valid variable name, an open parenthesis a variable declaration of the form "var variable_name" (where variable name is a valid variable name), using a space between parameters, an open curly bracket, a statement list, an optional return and a close parenthesis
	- example:
		def function(var x var y) {
			z = x + y;
			return z;
		}

- if, elif and else statements:
	- to write an if or elif statement, use the keywords "if" or "elif" followed by an open parenthesis, a conditional expression (of the form primary comparator primary ex. 10 < 15), a close parenthesis and a block (of the form { statement list })
	- an else should be written using the keyword "else" followed by a block
	
- while loop
	- to write a while loop, use the keyword "while" followed by an open parenthesis, a valid conditional espression, and a block
	- example:
		while (x < 10) {
			print(x);
			x = x + 1;
		}

- Arrays:
	- to create an array, use an expression of the form arr variable_name = []. Be sure to use the "arr" keyword.
	- to add an element to an array, assign the index you wish to add it to using an expression of the form 
		variable_name[primary] = primary;
	- to retrieve the value of an array at a given index, use an expression of the form variable_name[primary] or variable_name[integer], where the variable_name is the array's name

- more information about this language's syntax can be found in grammar.txt
