---- MODULE DashdotOp ----
-<caret>. a == 1

foo(a) == - a
foo(a, b) == a - b \* Checks that infix op isn't included as reference
==========================
