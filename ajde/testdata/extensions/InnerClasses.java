public class InnerClasses {
	
	public static void main(String[] args) {
		Runnable r = new Runnable() {
			public void run() {

			}
		};
		
		r.run();
		
		new Object() {
			public String toString() {
				return "a";
			}
		};
		
		new Runnable() {
			public void run() {

			}
		}.run();
	}
	
	
	static class A {
		public void method() {
			Runnable r = new Runnable() {
				public void run() {

				}
			};
		}
	}
}