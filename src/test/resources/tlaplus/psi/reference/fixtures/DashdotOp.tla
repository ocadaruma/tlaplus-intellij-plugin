---- MODULE DashdotOp ----
a - b == 42 \* Checks that prefix op doesn't resolve to infix operator definition
-. a == 1

foo(a) == <caret>- a
==========================
