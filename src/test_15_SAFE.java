public class test_15_SAFE {
public static void t15_safe() {
		MissileBattery b = new MissileBattery(6);
		MissileBattery c = new MissileBattery(5);
		MissileBattery a = b;
		
		int i = 5;
		
		a.fire(i);
	}
}
