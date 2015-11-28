package cryptolib;

public class PseudoRand {
	private APInt g, p, threshold, s;
	
	PseudoRand(APInt N){
		APInt[] a = APUtils.produceGenerator(N);
		g = a[0];
		p = a[1];
		threshold = p.div(new APInt(2))[0];	//Threshold for bit generation
	}
	void seed(APInt s){
		if(s.signedLT(APInt.zero) || !s.magLT(p))
			throw new ArithmeticException("Seeding pseudorandom generator with element outside group");
		this.s = s;
	}
	/*Returns the prime p defining the multiplicative group Z*|p*/
	APInt getPrime(){return p;}
	
	/*Generates a nice, fresh, random bit (assuming the DLA holds)*/
	int generateBit(){
		int out = 0;
		if(s == null){
			seed(APUtils.generateRandModN(p));
		}
		if(s.magGT(threshold))
			out = 1;
		s = g.modExp(s, p);
		return out;
	}
}
