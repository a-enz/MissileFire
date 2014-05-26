public class test_19_UNSAFE {
public static void t19_unsafe() {
		MissileBattery a = new MissileBattery(7);
		MissileBattery b = a;
		int i = 1;
		int j = 4;
		while(i < 4) {
			i++;
			j++;
		}
		b.fire(j);
	}
}
