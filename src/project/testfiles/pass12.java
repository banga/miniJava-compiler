class MainClass
{
	public static void main(String[] arg)
	{
		int y = 0;  
		for (System.out.println("init"); y < 2; System.out.println("incr"))
		{
			 y = y + 1;
		}
		
		System.out.println(y);
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