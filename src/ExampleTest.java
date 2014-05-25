public class ExampleTest {
	public static void t17_safe() {
		MissileBattery a = new MissileBattery(6);
		MissileBattery b = new MissileBattery(6);
		MissileBattery c = new MissileBattery(6);
		int i,j;
		if (b == c) {
			i = 3;
		} else {
			i = 5;
		}
		
		if (b == c) {
			j = 0;
		} else {
			j = 2;
		}
		
		a.fire(i);
		a.fire(j);
	}
}

