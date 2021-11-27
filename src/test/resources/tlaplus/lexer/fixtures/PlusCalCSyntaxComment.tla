-------- MODULE PlusCalCSyntaxComment --------
X == 1
(*
xxx
--algorithm Euclid {
  variables u = 24;
            v \in 1 .. N;
            v_init = v;
  {
    while (u # 0) {
      if (u < v) {
          u := v || v := u;
      };
      u := u - v;
    };
    print <<24, v_init, "have gcd", v>>
  }
}
yyy
*)
Y == 2
==============================================
