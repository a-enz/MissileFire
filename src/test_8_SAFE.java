public class test_8_SAFE {
public static void t8_safe() {
		MissileBattery b = new MissileBattery(6);
		int i = 2;
		int j = 2;
		b.fire(i * j + 1);
		b.fire(i * j - 4);
	}
}
