public class test_22_SAFE {
public static void t22_safe() {
		MissileBattery a = new MissileBattery(16);
		MissileBattery b = a;
		int i = 2;
		int r = 1;
		int n = 4;
		
		r = r + i;
		i++;
		r = r + i;
		i++;
		r = r + i;
		i++;
		r = r + i;
		i++;
		
		b.fire(r);
	}
}
