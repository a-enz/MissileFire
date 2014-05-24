public class ExampleTest {
	
	
//  public static void l() {
//	  MissileBattery a = new MissileBattery(5);
//	  int x = 5;
//	  int y = 0;
//	  while(y < x){
//		  y++;
//	  }
//	  a.fire(3);
//	  a.fire(4);
//  }
  
//  public static void m() {
//	  MissileBattery a = new MissileBattery(5);
//	  int x = 5;
//	  int y = 0;
//	  while(y < x){
//		  y++;
//	  }
//	  a.fire(4);
//	  a.fire(4);
//  }
  
  public static void l() {
      int k=5;
      int i=10;
      
      MissileBattery a = new MissileBattery(12);

      while(k < 5){
    	  k += 50;
      }
      
      a.fire(k);

  }
}

