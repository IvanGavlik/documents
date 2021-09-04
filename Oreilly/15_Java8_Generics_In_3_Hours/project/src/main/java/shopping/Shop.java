package shopping;

public class Shop {
  public static void main(String[] args) {
    ClothingPair<Sock> ps = new ClothingPair<>(new Sock(9, "Red"), new Sock(9, "Blue"));
    System.out.println("socks match? " + ps.isMatched());
    ClothingPair<Sock> ps2 = new ClothingPair<>(new Sock(10, "Red"), new Sock(9, "Red"));
    System.out.println("socks match? " + ps2.isMatched());

    System.out.println("proposed items match? "
        // explicit assignment--unnecessary, inferencing will do this :)
//        + ClothingPair.<Sock>match(new Sock(10, "Blue"), new Sock(11, "Blue")));
        + ClothingPair.match(new Sock(10, "Blue"), new Sock(11, "Blue")));
    //    ClothingPair<String> cps;

//    ClothingPair cp = ps;
    // if context is "vague enough" we actually canget
    // a type for s that is Sized & Colored
    var s = ps.getLeft();
  }
}
