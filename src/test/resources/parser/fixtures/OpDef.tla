---- MODULE OpDef ----
a+b == {a, b}
-. a == 0 - a
SeqMax(seq) == CHOOSE x \in seq: \A y \in seq: x >= y
CallAndAddSeven(op(_, _), p1, p2) ==
    LET CallResult == op(p1, p2)
    IN DEP!AddSeven(CallResult)
PlusFive == CallAndAddSeven(LAMBDA x, y: x + y, 4, 5)
======================
