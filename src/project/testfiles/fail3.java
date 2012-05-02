class MainClass
{
	public static void main(String[] arg)
	{
		Foo foo1 = new Foo();
		Foo foo2 = new Foo();
		Foo foo3 = foo1;
		Foo foo4 = null;
		if (foo1 == foo2)
		{
			System.out.println(2);
		}
		if (foo1 == foo3)
		{
			System.out.println(3);
		}
		if (foo4 == null)
		{
			System.out.println(4);
		}

		foo4.myFoo = foo4.test();
	
	}
}
class Foo
{
	int x;
	Foo myFoo;
	Foo test()
	{
		return null;
	}
}