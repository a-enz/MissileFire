public class ExampleTest {

	public static void t14_unsafe() {
		MissileBattery b = new MissileBattery(6);
		
		int i = 0;
		while (i != 0) {
			i++;
			i--;
		}
		
		b.fire(10);
	}
}

