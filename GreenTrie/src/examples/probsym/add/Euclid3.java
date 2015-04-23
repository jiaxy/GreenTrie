package probsym.add;

class Euclid3 {

	public static int gcd(int a, int b) {
		if(a==b){
			return a;
		}
		if (a == 0) {
			return b;
		}
		
		while (b != 0&&a>0&&a!=b) {
			if (a > b) {
				a = a - b;
			} else {
				b = b - a;
			}
		}
		return a;
	}

	public static void main(String[] Argv) {
		int x = gcd(105, 252);
		System.out.println("gcd(195, 252) == " + x);
//		gcd(105, 252);
	}

}
