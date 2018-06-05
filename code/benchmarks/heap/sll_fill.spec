/** A node in a singly-linked list.
 */
type SLL {
  n:SLL
  d:int
}

/** Assigns the given value to each element of the list referenced by head.
 */
fill(head:SLL, val:int) -> () {
  var t:SLL

/*
  example {
    [head==null && val==42]
    -> t = head;
  }
*/

  example {
    [head==o1 && o1.n==o2 && o2.n==null && val==42]
    -> t = head;
    -> t.d = val;
    -> t = t.n;
    -> t.d = val;
    -> t = t.n;
  }

/* planning-driven examples:
  example {
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==null && val==42] ->
    [t==o1] ->
    [o1.d==42] ->
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==null &&
     o1.d==42 && o2.d==42 && o3.d==42 && o4.d==42]
  }
*/  
}