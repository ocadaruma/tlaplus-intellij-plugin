--fair algorithm HyperEuclid {
    variables x \in 1..M, y \in 1..N,
              x0 = x, y0 = y;
  {
    while (x # y) {
      if (x < y) {
        y := y - x;
      } else {
        x := x - y;
      }
    };
    assert (x = y) /\ (x = GCD(x0,y0));
  }
}
