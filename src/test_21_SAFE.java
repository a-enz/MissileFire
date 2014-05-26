public class test_21_SAFE {
public static void t21_safe() {
		MissileBattery a = new MissileBattery(4);
		MissileBattery b = a;
		int i = 1;
		int j = 2;
		while(i + j < 4) {
			i++;
			j--;
		}
		b.fire(5);
	}
}
