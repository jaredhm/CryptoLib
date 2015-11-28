package cryptolib;

public class APInt {
	final static int base = 10;
	final static APInt zero = new APInt(0);
	final static APInt one = new APInt(1);
	private int[] digits;
	private boolean sign;
	
	public int length;
	
	public APInt(int[] digits, boolean sign){
		this.digits = digits;
		length = digits.length;
		this.sign = sign;
	}
	public APInt(byte i8){buildDigits(i8);}
	public APInt(short i16){buildDigits(i16);}
	public APInt(int i32){buildDigits(i32);}
	public APInt(long i64){buildDigits(i64);}
	
	public boolean getSign(){return sign;}
	
	private void setSign(boolean sign){this.sign = sign;}
	
	public void set(int digit, int position){
		digits[position] = digit;
	}
	
	public int get(int position){
		return digits[position];
	}
	
	//Trims any leading zeros
	public void trim(){
		int len = length;
		int i = 1;
		while(i < length){
			if(digits[length - i] == 0)
				len--;
			else
				break;
			i++;
		}
		int[] trimmedDigits = new int[len];
		for(i = 0; i < len; i++){
			trimmedDigits[i] = digits[i];
		}
		length = len;
		digits = trimmedDigits;
	}
	
	/**Functions ignore sign and just evaluate magnitude of two APInts**/
	public boolean magGT(APInt api){
		if(api.length > length)
			return false;	//Here, it is assumed that APInts always remain trimmed
		if(api.length < length)
			return true;
		int i = length-1;
		while(i >= 0){
			if(digits[i] > api.get(i))
				return true;
			if(digits[i] < api.get(i))
				return false;
			else
				i--;
		}
		return false;
	}
	
	public boolean magLT(APInt api){
		if(api.length > length)
			return true;	//Here, it is assumed that APInts always remain trimmed
		if(api.length < length)
			return false;
		int i = length-1;
		while(i >= 0){
			if(digits[i] > api.get(i))
				return false;
			if(digits[i] < api.get(i))
				return true;
			else
				i--;
		}
		return false;
	}
	
	public boolean magEQ(APInt api){
		if(api.length != length)
			return false;
		int i = length-1;
		while(i >= 0){
			if(digits[i] != api.get(i))
				return false;
			i--;
		}
		return true;
	}
	
	public boolean singedGT(APInt api){
		if(!sign && api.getSign())
			return true;
		if(sign && !api.getSign())
			return false;
		return sign ^ this.magGT(api);
	}
	
	public boolean signedLT(APInt api){
		if(!sign && api.getSign())
			return false;
		if(sign && !api.getSign())
			return true;
		return sign ^ this.magLT(api);
	}
	
	public boolean signedEQ(APInt api){
		if(sign != api.sign)
			return false;
		return this.magEQ(api);
	}
	
	private void buildDigits(long n){
		int len = 0;	//Length of largest n will be around 20 decimal digits
		int digit;
		long k = (n >= 0) ? n : -n;	//Make k the absolute value of n
		do{
			len++;
 			digit = (int)(k % base);
			k = (k-digit)/10;
		}while(k > 0);
		digits = new int[length = len];	//Once length has been established, create structure
		int i = 0;
		k = (n >= 0) ? n : -n;
		do{
			digits[i++] = digit = (int)(k % base);
			k = (k-digit)/10;
		}while(k > 0);
		sign = (n < 0);
	}
	public String toString(){
		String out = (sign) ? "-" : "";
		int i = length;
		while(i > 0){out += digits[--i];}
		return out;
	}
	public APInt plus(APInt api){
		APInt result;
		if(!(sign ^ api.sign)){
			int carry = 0;
			APInt shorter = (length < api.length) ? (this) : (api);
			APInt longer = (length > api.length) ? (this) : (api);
			result = new APInt(new int[longer.length + 1], sign);	//At most, the result will have length+1 or api.length+1 digits, depending on which is larger
			int i;
			for(i  = 0; i < shorter.length; i++){
				int digit = digits[i] + api.get(i) + carry;
				result.set(digit % base, i);
				carry  =  (digit >= base) ? 1 : 0;
			}
			for(;i < longer.length; i++){
				result.set((longer.get(i) + carry) % base, i);
				carry = (longer.get(i) + carry >= base) ? 1 : 0;
			}
			result.set(carry, (result.length-1));
			result.trim();
		}
		else{
			APInt minuend;
			APInt subtrahend;
			if(sign){	//More convenient if the negative number is second so that they can effectively be subtracted
				minuend = api;
				subtrahend = this;
			}
			else{
				minuend = this;
				subtrahend = api;
			}
			if(subtrahend.magGT(minuend)){
				result = subtrahend.magDiff(minuend);
				result.setSign(true);
			}
			else
				result = minuend.magDiff(subtrahend);
		}
		return result;
	}
	
	/**Computes the difference (ignoring sign) between this APInt and the input APInt. 
	 * Caller APInt is assumed to be larger in magnitude (and thus no shorter in length than input)**/
	private APInt magDiff(APInt api){
		APInt me = getCopy(false);
		APInt result = new APInt(new int[length],false);
		int i;
		for(i = 0; i < api.length; i++){
			if(api.get(i) > me.get(i)){	//Perform carry
				me.set(me.get(i) + base,i);
				me.set(me.get(i+1)-1, i+1);
			}
			result.set(me.get(i)-api.get(i),i);
		}
		for(;i < length;i++){
			result.set(me.get(i), i);
		}
		result.trim();
		return result;
	}
	
	/**Compute product of APInt and a machine-precision number. Could be used for multiplication by 32-bit 
	 * of any size, but will only be used for single digits.**/
	public APInt partialProduct(int num){
		APInt result;
		int digit;
		int carry;
		int absNum = (num < 0) ? -num : num;
		result = new APInt(new int[length+1],false);
		for(int i = 0; i < length; i++){
			result.set(result.get(i) + digits[i]*absNum, i);
			if(result.get(i) >= base){
				digit = result.get(i) % base;
				carry = (result.get(i) - digit)/base;
				result.set(digit, i);
				result.set(carry, i+1);
			}
		}
		result.trim();
		return result;
	}
	
	/*So simple, I almost didn't write this*/
	public APInt abs(){ return getCopy(sign); }
	
	public APInt getCopy(boolean negate){
		APInt xerox;
		int[] digs = new int[length];
		System.arraycopy(digits, 0 , digs , 0, length);
		xerox = new APInt(digs,sign^negate);
		return xerox;		
	}
	
	public APInt times(APInt api){
		APInt result = new APInt(new int[api.length + length],false);
		APInt prod;
		int digit;
		for(int i = 0; i < api.length; i++){
			digit = api.get(i);
			prod = partialProduct(digit);
			prod = prod.shiftLeft(i);
			result = result.plus(prod);
		}
		result.setSign(sign^api.sign);
		return result;
	}
	
	/*Computes euclidean division of two integers, this, and api. Returns a pair (q,r) = (floor(this/api),this mod api)
	 * such that 0 <= r < |api|
	 */
	public APInt[] div(APInt api) throws ArithmeticException {
		APInt[] out = new APInt[2];
		if(api.magEQ(zero))
			throw new ArithmeticException("Divide by zero");
		if(this.magEQ(zero)){
			out[0] = zero;
			out[1] = api.getCopy(false);
		}
		else{
			out = abs().divSub(api.abs());
			if(sign && !(out[1].magEQ(zero))){	//Need to adjust divisor if dividend is zero 
				out[0] = out[0].plus(one);		//Bump q up by 1 to account for positive remainder
				out[1] = api.abs().times(out[0]).plus(this); 
			}
			out[0] = out[0].getCopy(sign^api.getSign()^out[0].magEQ(zero));	//Negate q (unless its 0) if the divisor is negative		
		}
		return out;
	}
	
	/*Computes the parity of this */
	private boolean isEven(){return (digits[0] % 2 == 0);}
	
	/*Computes a pair of integers (q,r) by the sign-agnostic Euclidean division of this and api 
	 * subject to 0 <= r < |api|
	 * */
	public APInt[] divSub(APInt api){
		APInt[] out = {zero, zero};
		APInt rem = getCopy(false);
		APInt div = api.getCopy(false);
		APInt shiftedDiv = div.getCopy(false);
		int remMsds, divMsd, mul, pow;
		pow = 0;
		while(rem.magGT(api) || rem.magEQ(api)){
			if(rem.magEQ(api)){
				rem = zero.getCopy(false);
				out[0] = out[0].plus(one);
			}
			else{ 
				if(rem.length == api.length){
					shiftedDiv = div;	//No need to shift, only subtract
					remMsds = rem.get(rem.length-1);
					divMsd = div.get(div.length-1) + 1;
					if(remMsds < divMsd)
						divMsd--;	//On the off-chance they both begin with the same digit
				}
				else{
					remMsds = (rem.get(rem.length-1) * base) + rem.get(rem.length-2);
					pow = rem.length - div.length - 1;
					divMsd = div.get(div.length-1) + 1;	//Here, we basically round the msd of the divisor up, to ensure the remainder doesn't go negative
					shiftedDiv = div.shiftLeft(pow);
	
				}
				mul = (remMsds - (remMsds % divMsd))/divMsd;	//mul = floor(remMsds/divMsd)
				rem = rem.plus(shiftedDiv.times(new APInt(mul)).getCopy(true));	//rem = rem - div*mul. This step may look quadratic in the 
																										//length of the input, but mul has at most 2 digits
				out[0] = out[0].plus(new APInt(mul).shiftLeft(pow));
			}
		}
		out[1] = rem;
		return out;
	}
	
	/*Computes this^b mod N per Katz and Lindell's description of the ModExp algorithm*/
	public APInt modExp(APInt b, APInt N){
		APInt out;
		APInt t;
		if(b.magEQ(one))
			out = this.getCopy(false);
		else{
			if(b.isEven()){
				t = modExp(b.div(new APInt(2))[0], N);
				out = t.times(t).div(N)[1];	//Return t^2 mod N
			}
			else{
				t = modExp(b.plus(new APInt(-1)).div(new APInt(2))[0],N);	//How ugly is this? Reads t = (a^(b-1)/2) mod N
				out = this.times(t).times(t).div(N)[1];		//Return at^2 mod N
			}
		}
		return out;
	}
	
	/*Euclid's algorithm for computing the gcd of this and input APInt api*/
	public APInt gcd(APInt api) throws ArithmeticException{
		if(api.magEQ(zero))
			throw new ArithmeticException("Computing gcd with zero");
		if(api.getSign() || sign)
			throw new ArithmeticException("Computing gcd with negative integer");
		if(this.magLT(api))
			return api.gcd(this);	//Insist caller >= input
		if(api.div(this)[0].magEQ(zero))
			return api;
		else
			return api.gcd(div(api)[1]);	//Return gcd(api,this mod api)
	}

	/*Per Miller-Rabin*/
	public boolean isPrime(){
		if(this.signedLT(new APInt(2)))
			throw new ArithmeticException("Primality test on integer less than 2");
		if(this.magEQ(new APInt(2)))
			return true;
		if(this.isEven())
			return false;	//Ok that covers half of them
		//Now compute this - 1 = (2^r)*u
		APInt n = this.plus(new APInt(-1));
		APInt u = n.div(new APInt(2))[0];
		APInt r = one;
		
		while(u.isEven()){
			r = r.plus(one);
			u = u.div(new APInt(2))[0];
		}
		/*Choosing 2^(-10) as the probability that this algorithm doesn't work*/ 
		APInt a;
		boolean next = false;
		for(int j = 0; j < 10; j++){
			while((a = APUtils.generateRandModN(n)).signedLT(zero));		//Try generating a positive random number until it works
			APInt x = a.modExp(u, this);
			if(x.magEQ(one) || x.magEQ(n))
				continue;
			//Test for being a strong witness
			for(APInt i = zero.getCopy(false);i.magLT(r);i = i.plus(one)){
				x = x.modExp(new APInt(2), this);
				if(x.magEQ(one))
					return false;
				if(x.magEQ(n)){
					next = true;	//Skip to next outermost loop
					break;
				}
			}
			if(!next)
				return false;
		}
		return true;
	}
	
	/**Essentially multiplies this APInt by the (base) raised to the second argument**/
	private APInt shiftLeft(int pow){
		int[] shiftedDigits = new int[length + pow];
		for (int i = length + pow -1; i >= pow; i--)
			shiftedDigits[i] = digits[i-pow];
		return new APInt(shiftedDigits,sign);
	}
	
	private APInt shiftRight(int pow){
		int[] shiftedDigits = new int[length - pow];
		for(int i = pow; i < length; i++)
			shiftedDigits[i - pow] = digits[i];
		return new APInt(shiftedDigits,sign);
	}
	
	/**Zereos out 'len' number of digits starting from the right**/
	private APInt maskRight(int len){
		int[] maskedDigits = new int[length];
		for(int i = length-1; i >= len; i--)
			maskedDigits[i] = digits[i];
		return new APInt(maskedDigits,sign);
	}
	/**Trims 'len' digits starting from the left**/
	private APInt maskLeft(int len){
		int[] maskedDigits = new int[length - len];
		for(int i = 0; i < length-len; i++)
			maskedDigits[i] = digits[i];
		return new APInt(maskedDigits,sign);
	}

}
