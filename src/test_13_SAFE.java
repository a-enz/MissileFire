public class test_13_SAFE {
public static void t13_safe() {
		MissileBattery b = new MissileBattery(6);
		
		int i = 0;
		while (i == 0) {
			i++;
			i--;
		}
		
		b.fire(10);
	}
}
