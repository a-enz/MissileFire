public class ExampleTest {
	
	
  public static void l() {
      int k=0;
      int i=10;
      
      MissileBattery a = new MissileBattery(12);
      MissileBattery b = a;

      while(k < 5){
    	  i += 2;
    	  k++;
      }
      
      MissileBattery c = new MissileBattery(20);
      b = c;
      
      a.fire(k);
      b.fire(25);
  }
}

