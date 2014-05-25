public class test_20_UNSAFE {
public static void t20_unsafe() {
		MissileBattery a = new MissileBattery(9);
		MissileBattery b = a;
		int i = 1;
		int j = 0;
		while(i < 10) {
			i++;
			j++;
		}
		b.fire(j);
	}
}
