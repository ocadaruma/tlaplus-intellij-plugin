---------- MODULE case -----------
Foo == CASE
          \/ foo ->
         /\ bar
       [] OTHER -> baz
==================================
