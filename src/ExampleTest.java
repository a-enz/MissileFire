public class ExampleTest {
  public static void l() {
	  
	  MissileBattery rawr = new MissileBattery(12);
	  int x = 5;
	  int y = 7;
	  
	  while (x > 0){
		  y = y + 7;
		  x = x - 1;
	  }
	  
	  rawr.fire(y);
  }
}

