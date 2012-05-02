class MainClass
{
	public static void main(String[] arg)
	{
		Foo foo1 = new Foo();
		Foo foo2 = new BabyFoo(); 
	}
}
class Foo
{
	int x;
	Foo myFoo;
	Foo test()
	{
		return this;
	}
}
class BabyFoo extends Foo
{
	int y;
	Foo test()
	{
		return myFoo;
	}
}
