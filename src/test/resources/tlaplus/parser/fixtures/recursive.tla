---- MODULE recursive ----
RECURSIVE RecOp(_, _)
RecOp(a, b) == IF (a > 0) \/ (b > 0) THEN RecOp(a - 1, b - 1) ELSE a + b
====
