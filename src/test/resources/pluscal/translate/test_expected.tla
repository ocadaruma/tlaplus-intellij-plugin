------------------------- MODULE test -------------------------
EXTENDS Integers, Sequences

(*--algorithm test
variables
    x = "",
procedure foo() begin
    Foo:
        x := self;
        return;
end procedure;
process baz = "One"
begin Baz:
    call foo()
end process
end algorithm;*)
\* BEGIN TRANSLATION (chksum(pcal) = "83c39aa4" /\ chksum(tla) = "3e50108c")
VARIABLES pc, x, stack

vars == << pc, x, stack >>

ProcSet == {"One"}

Init == (* Global variables *)
        /\ x = ""
        /\ stack = [self \in ProcSet |-> << >>]
        /\ pc = [self \in ProcSet |-> "Baz"]

Foo(self) == /\ pc[self] = "Foo"
             /\ x' = self
             /\ pc' = [pc EXCEPT ![self] = Head(stack[self]).pc]
             /\ stack' = [stack EXCEPT ![self] = Tail(stack[self])]

foo(self) == Foo(self)

Baz == /\ pc["One"] = "Baz"
       /\ stack' = [stack EXCEPT !["One"] = << [ procedure |->  "foo",
                                                 pc        |->  "Done" ] >>
                                             \o stack["One"]]
       /\ pc' = [pc EXCEPT !["One"] = "Foo"]
       /\ x' = x

baz == Baz

(* Allow infinite stuttering to prevent deadlock on termination. *)
Terminating == /\ \A self \in ProcSet: pc[self] = "Done"
               /\ UNCHANGED vars

Next == baz
           \/ (\E self \in ProcSet: foo(self))
           \/ Terminating

Spec == Init /\ [][Next]_vars

Termination == <>(\A self \in ProcSet: pc[self] = "Done")

\* END TRANSLATION 
=============================================================================
