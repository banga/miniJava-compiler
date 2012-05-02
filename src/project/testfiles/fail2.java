class MainClass
{
 	String y;
	public static void main(String[] arg)
	{
		String x = null;
		Foo m = null;
		m = new Foo();
		
 		m.myFoo = m.test();
		if (m.myFoo.number == 0)
		{
			System.out.println(0);
		}
		 
	}
}

class Foo
{
	Foo myFoo;
	int number;
	Foo test()
	{
		return null;
	}
}