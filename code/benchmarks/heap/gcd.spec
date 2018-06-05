/** The Euclidian algorithm for the greatest common divisor
 * of two positive integers.
 */
gcd(mut a:int, mut b:int) -> (res:int) {
  while (a != b) {
    if (a > b) {
      a = a - b;
    }
    else {
      b = b - a;
    }
  }
  res = a;

  example {
    [a==100 && b==150]
    -> b = b - a; // a==100 && b==50
    -> a = a - b; // a==50 && b==50
    -> res = a;
  }

  example {
    [a==130 && b==75]
    -> a = a - b; // a==55 && b==75
    -> b = b - a; // a==55 && b==20
    -> a = a - b; // a==35 && b==20
    -> a = a - b; // a==15 && b==20
    -> b = b - a; // a==15 && b==5
    -> a = a - b; // a==10 && b==5
    -> a = a - b; // a==5 && b==5
    -> res = a;
  }

  test example {
    [a==16 && b==12] -> ...
  }

  test example {
    [a==20 && b==12] -> ...
  }

  test example {
    [a==12 && b==16] -> ...
  }

  test example {
    [a==12 && b==20] -> ...
  } 
}