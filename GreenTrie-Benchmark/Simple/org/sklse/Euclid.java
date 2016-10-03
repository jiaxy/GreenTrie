package org.sklse;


class Euclid {

	public static int gcd(int a, int b) {
		if (a == 0) {
			return b;
		}
		while (b != 0) {
			if (a > b) {
				a = a - b;
			} else {
				b = b - a;
			}
		}
		return a;
	}

	public static void main(String[] Argv) {
		int x = gcd(195, 252);
		System.out.println("gcd(195, 252) == " + x);
//		gcd(105, 252);
	}

}
