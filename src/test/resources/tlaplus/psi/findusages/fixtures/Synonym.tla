---- MODULE Synonym ----
a \l<caret>eq b == 1

foo(a, b) == a =< b
foo(a, b) == a <= b
foo(a, b) == a \leq b
========================
