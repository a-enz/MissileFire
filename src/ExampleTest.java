public class ExampleTest {
  public static void l() {
	  
	  MissileBattery rawr = new MissileBattery(12);
	  int x = 5;
	  int y = 7;
	  
	  if (x > 0){
		  x = (x + 4) * (y - 1);
	  }
	  
	  rawr.fire(y);
  }
}

