public class test_12_UNSAFE {
public static void t12_unsafe() {
		MissileBattery b = new MissileBattery(6);
		int i = 8;
		int j = 5;
		if (i >= j) {
			b.fire(8);
		}
	}
}
