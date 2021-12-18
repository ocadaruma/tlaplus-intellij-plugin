---- MODULE Foo ----
EXTENDS Bar, Sequences

\*F == [a \in {1, 3} |-> "x"]
S == Append([a \in {1, 3} |-> "x"], "y")
====
