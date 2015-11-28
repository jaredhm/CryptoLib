package cryptolib;
import java.util.Random;

public class APUtils {
	
	/*Generates a random natural number of length len*/
	public static APInt generateRand(int len) throws ArithmeticException{
		if(len < 1)
			throw new ArithmeticException("Generating a number of length less than 1");
		int[] digs = new int[len];
		for(int i = 0; i < len; i++)
			digs[i] = generateRandDigit();
		APInt out = new APInt(digs,false);
		out.trim();
		return out;
	}
	
	/*Returns a random number in [0,N-1], also we insist that N isn't less than 1. 
	 * Returns -1 on failure to generate a nice random number*/
	public static APInt generateRandModN(APInt N) throws ArithmeticException{
		if(N.signedLT(APInt.one))
			throw new ArithmeticException("Generating random number with modulus less than 1");
		int n = N.length;
		APInt x;
		for(int i = 0; i < 2*n; i++){
			x = generateRand(n);
			if(x.magLT(N))
				return x;
		}
		return new APInt(-1);
	}
	
	/*Per Adam Kalai 2003*/
	public static APList generateFactors(APInt N){
		APList seq,out;
		APInt s, temp, r;
		boolean repeat = true;
		do{
			s = N.getCopy(false);
			seq = new APList();
			r = APInt.one;
			while(true){
				while(true){	//Generates a random between 1 and s, inclusive
					temp = generateRandModN(s.plus(APInt.one));
					if(temp.singedGT(APInt.zero))
						break;
				}
				s = temp;
				if(s.magEQ(APInt.one))
					break;
				seq.addFront(s);	//Add to list of possible prime factors
			}
			s = seq.newIterator();
			out = new APList();
			do{
				if(s.isPrime()){
					out.addFront(s);
					r = r.times(s);	//Write r = s_1*s_2*...*s_l
				}
			}while((s = seq.next()) != null);
			APInt test;
			while(true){
				test = generateRandModN(N);
				if(!test.signedLT(APInt.zero))
					break;
			}
			repeat = !(!r.magGT(N) && r.magGT(test));	//'test' is uniform in [0,...,N-1], hence r > test w/ probability r/N
		}while(repeat);	
		out.addFront(r);	//Finally, add r
		return out;
	}	
	
	/*Generates a prime m+1 and the prime factorization of m*/
	public static APList generateFactoredPrime(APInt N){
		APList out;
		APInt m;
		while(true){
			out = generateFactors(N);
			m = out.popFront();
			if(m.plus(APInt.one).isPrime()){
				out.addFront(m.plus(APInt.one));	//Add p = m+1 to data structure
				break;
			}
		}
		return out;
	}
	
	/*Returns a generator g and a prime p defining the multiplicative group Z*|p*/
	public static APInt[] produceGenerator(APInt N){
		APList fact = generateFactoredPrime(N);
		APInt p = fact.popFront();
		APInt m = p.plus(new APInt(-1));	//m = p-1
		APInt g, a, q;
		g = APInt.zero;
		boolean repeat = true;
		while(repeat){
			repeat = false;
			g = generateRandModN(p);
			q = fact.newIterator();
			do{
				a = m.div(q)[0];	//Get a = m/q where q is a factor of m = p-1
				if(g.modExp(a, p).magEQ(APInt.one)){
					repeat = true;
					break;
				}
			}while((q = fact.next())!=null);
		}
		APInt[] out = {g,p};
		return out;
	}
	
	private static int generateRandDigit(){
		Random r = new Random();
		return r.nextInt(10);
	}

}
