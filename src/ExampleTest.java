public class ExampleTest {

  public static void l() {
	  int x = 10;
	  int i = 0;
	  
	  MissileBattery a = new MissileBattery(20);
	  MissileBattery b = new MissileBattery(14);
	  
	  while (i < 3){
		  x++;
		  i++;
	  }
	  MissileBattery r = new MissileBattery(4);
	  r.fire(i);
	  a.fire(12);
	  b.fire(x);

  }
}

