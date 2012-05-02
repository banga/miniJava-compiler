class MainClass
{
	public static void main(String[] arg)
	{
		int y = 0;  
		for (int x = 0; x < 2; x = x + 1)
		{
			y = y + 1;
			y = y + 2;
		}
		int x = 0;
		while ( y < 2)
		{
			y = y + 1;
			x = x + 1;
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