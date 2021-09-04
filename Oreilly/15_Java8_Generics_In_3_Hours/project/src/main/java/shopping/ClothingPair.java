package shopping;

//public class ClothingPair<E extends Colored, String> extends Pair<E> {
//  java.lang.String s = "Hello";
// Multiple constraints *can* include ONE object type, if so, it must be first in the list.
public class ClothingPair<E extends Object & Colored & Sized> extends Pair<E> {
  public ClothingPair(E left, E right) {
    super(left, right);
  }

  // send command argument here.
  public boolean isMatched() {
//    E targetLeft = left;
  // could be.. (isn't) Sized & Colored
//    var targetLeft = getLeft();
//    if (left instanceof Sock && right instanceof Sock) // YUK!!!!
//    if (left instanceof Colored && right instanceof Colored) // still YUK!!!!
    return left.getColor().equals(right.getColor())
        && left.getSize() == right.getSize();
  }

  public static <F extends Colored & Sized> boolean match(F left, F right) {
    return left.getColor().equals(right.getColor())
        && left.getSize() == right.getSize();
  }

}
