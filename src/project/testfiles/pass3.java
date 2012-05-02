class MainClass
{
	public static void main(String[] arg)
	{
		Foo foo1 = new Foo();
		Foo foo2 = new Foo(); 
		foo1.myFoo = foo2;
		Foo foo3 =	 foo1.test(false);// foo3 == foo1
		Foo foo4 = foo1.test(true); // foo4 == foo2
		if (foo3 == foo1)
			System.out.println("foo3 is foo1");
		if (foo4 == foo1)
			System.out.println("foo4 is foo1");
		else if (foo4 == foo2)							 		
			System.out.println("foo4 is foo2");
		foo4 = foo1.test(1);
		if (foo4 == foo1)
			System.out.println("Second try: foo4 is foo1");	
		else 
		System.out.println("Second try: foo4 is NOT foo1"); 
		
		
			
	}
}
class Foo
{
	int x;
	Foo myFoo;
	Foo test(int val)
	{
		return this;
	}
	Foo test(boolean returnOther)
	{
		Foo returnVal = null;
		if (returnOther)
		{
			returnVal = myFoo;
		}
		else
		{
			returnVal = this;
		}
		return returnVal;
	}
}
 
