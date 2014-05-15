public class ExampleTest {
  public static void l() {
	  
	  MissileBattery rawr = new MissileBattery(12);
	  int x = 5;
	  int y = 7;
	  
	  while (x < y){
		  x = x + 1;
		  
		  while(x == 5){
			  y = 10;
		  }
	  }
	  rawr.fire(y);
  }
}

