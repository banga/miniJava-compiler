class MainClass
{
 	String y;
	public static void main(String[] arg)
	{
		//int null = 9;
		//System.out.println(null);
		//int x = null;
		//String hello = null;
		//null.hello;
		//System.out.println(null);
		String x = null;
		Foo m = new Foo();
		
 		m.myFoo = m.test();
			
		
		//String y = new String();
		//x = y;
		//if (x == null)
	//	{
		//	System.out.println("x is null");
		//}
	//	if (y == null)
		//{
	   //   System.out.println("y is null");
		//}
	}
}

class Foo
{
	Foo myFoo;
	Foo test()
	{
		return null;
	}
}