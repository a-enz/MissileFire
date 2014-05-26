public class test_10_SAFE {
public static void t10_safe() {
		MissileBattery b = new MissileBattery(6);
		int i = 8;
		int j = 5;
		if (i == j) {
			b.fire(8);
		}
	}
}
