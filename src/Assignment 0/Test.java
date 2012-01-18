interface I1 {int x = 0;}
class T1 implements I1 {int x = 1;}
class T2 extends T1 {int x = 2;}
class T3 extends T1 {
  int x = 3;
  void test() {
    /* Prints 0 */
    System.out.println(I1.x);

    /* Prints 1 */
    System.out.println(super.x);
    
    /* Prints 2 */
    System.out.println(new T2().x);
    
    /* Prints 3 */
    System.out.println(x);
  }
}

class Test {
  public static void main(String[] args) {
    new T3().test();
  }
}
