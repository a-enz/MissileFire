public class test_6_SAFE {
public static void t6_safe() {
		MissileBattery b = new MissileBattery(6);
		int i = 5;
		b.fire(i);
		i = 0;
		b.fire(i);
	}
}
