package cryptolib;

public class Main {
	public static void main(String[] args){
		long start = System.currentTimeMillis();
		APInt test = APUtils.generateRand(16);
		PseudoRand r = new PseudoRand(test);
		System.out.println("Z*/p defined for p = " + r.getPrime());
		for(int i = 0; i < 100; i++)
			System.out.print(r.generateBit());
		System.out.println("\nTime in seconds: " + (System.currentTimeMillis() - start)/1000);
	}
}
