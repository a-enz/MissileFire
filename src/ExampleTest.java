public class ExampleTest {
	
	
  public static void l() {
	  
	  
	  MissileBattery n = new MissileBattery(12);
	  int x = 5;
	  int y = 11;
	  

	  if (x < 100){
		  y = 0;
		  x = 7 + y;
		  y = x * 10;
	  }
	  
	  n.fire(x);
  }
}

