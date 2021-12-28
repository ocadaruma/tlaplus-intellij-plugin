---- MODULE Foo ----
EXTENDS Bar, Sequences

F == [a \in {1, 3} |-> "x"]
S == Append(F, "y")
====
