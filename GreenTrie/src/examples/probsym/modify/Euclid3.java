package probsym.modify;

class Euclid3 {

	public static int gcd(int a, int b) {
		if (a <=1) {
			return b;
		}
		while (b > 0) {
			if (a > b) {
				a = a - b;
			} else {
				b = b - a-1;
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
