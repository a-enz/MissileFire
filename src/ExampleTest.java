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
	  a.fire(19);
	  a.fire(19);
	  b.fire(x);

  }
}

