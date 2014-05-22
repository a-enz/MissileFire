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
      
      a.fire(k);
      b.fire(13);
  }
}

