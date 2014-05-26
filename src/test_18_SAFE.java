public class test_18_SAFE {
public static void t18_safe() {
		MissileBattery a = new MissileBattery(7);
		MissileBattery b = a;
		int i = 1;
		int j = 3;
		while(i < 4) {
			i++;
			j++;
		}
		b.fire(j);
	}
}
