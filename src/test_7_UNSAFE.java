public class test_7_UNSAFE {
public static void t7_unsafe() {
		MissileBattery b = new MissileBattery(6);
		int i = 5;
		b.fire(i);
		i = 0;
		b.fire(i - 1);
	}
}
