public class test_11_SAFE {
public static void t11_safe() {
		MissileBattery b = new MissileBattery(6);
		int i = 8;
		int j = 5;
		if (i <= j) {
			b.fire(8);
		}
	}
}
