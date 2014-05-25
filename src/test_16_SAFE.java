public class test_16_SAFE {
public static void t16_safe() {
		MissileBattery b = new MissileBattery(5);
		b = new MissileBattery(6);
		MissileBattery a = b;
		
		int i = 5;
		
		a.fire(i);
	}
}
