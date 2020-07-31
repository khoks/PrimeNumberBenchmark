package com.khoks.test.experiments;

public class StringBasedPrimeNumberGenerator {

	public static void main(String[] args) {
		//String dividend = "902183091289302189038";
		//String divisor = "23987479832478923";
		
		String dividend = "1248163264128256512"; 
		double divisor = 125; 
		System.out.println(longDivision(dividend, divisor)); 
	}

	
	public static String longDivision( 
	        String number, 
	        double divisor) 
	    { 
	  
	        StringBuilder result = new StringBuilder(); 
	  
	        char[] dividend = number.toCharArray(); 
	  
	        double carry = 0; 
	  
	        for ( int i = 0; i < dividend.length; i++) { 
	            // Prepare the number to be divided 
	            double x = (carry * 10d) + Character.getNumericValue(dividend[i]); 
	  
	            // Append the result with partial quotient 
	            result.append(x / divisor); 
	  
	            // Prepare the carry for the next Iteration 
	            carry = x % divisor; 
	        } 
	  
	        // Remove any leading zeros 
	        for ( int i = 0; i < result.length(); i++) { 
	            if (result.charAt(i) != '0') {
	                return result.substring(i); 
	            } 
	        } 
	        return ""; 
	    }
}
