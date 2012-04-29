class A {
	int x = 1;
	A() {
		System.out.println("A()");
	}
	
	String toString() {
		return "A";
	}
}
class B extends A {
	int x = 3;

	B() {
		System.out.println(x);
	}

	String toString() {
		return "B";
	}
	
	public static void main(String[] arg) {
		C c = new C();
		c.test();
	}
}

class C extends B {
	private int y = 5;

	C() {
		System.out.println("C()");
	}

	String toString() {
		return "C";
	}

	void test() {
		System.out.println(x);
		System.out.println(y);

		T t = new T("hi");
		System.out.println(t.b.x);
		System.out.println(t.toString());
		
		System.out.println("****");
		
		Object[] arr = new Object[4];
		arr[0] = new A();
		arr[1] = new B();
		arr[2] = new C();
		arr[3] = new T("test");
		
		for(int i = 0; i < arr.length; i = i + 1) {
			Object a = arr[i];
			System.out.println(a.toString());
		}
	}
}

class T {
	String name = "shrey";
	B b = new B();
	
	T(String str) {
		name = str;
	}
	
	String toString() {
		return name;
	}
}