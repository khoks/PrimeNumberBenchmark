package com.khoks.test.experiments;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BigPrimeNumberGenerator {

	private static final BigInteger START_NUM_RANGE = new BigInteger("100000000");
	private static final BigInteger END_NUM_RANGE = new BigInteger("10000000000");
	private static BigInteger threadNumBatchSize = new BigInteger("100");
	private static int executorPoolSize = 1;
	private static int executorInputPoolSize = executorPoolSize * 1; 
	public static final double NANO_TO_SECOND_RATIO = 1000000000d;
	
	public static void main(String[] args) {
		System.out.println("Generating Prime numbers starting from " + START_NUM_RANGE);
		BigPrimeNumberGenerator test1Obj = new BigPrimeNumberGenerator();		
		test1Obj.threadExecuter();
	}
	
	public void threadExecuter() {
		ExecutorService executor = Executors.newFixedThreadPool(executorPoolSize);
		
		BigInteger i = BigInteger.ZERO ;
		double startTime  = System.nanoTime(), endTime;
		Set<Future> threadResults = new HashSet<Future>();
		boolean canInsert = true;
		double primeCount = 0d;
		double totalPrimeTime = 0d;
		
		for(i = START_NUM_RANGE ; i.compareTo(END_NUM_RANGE) < 0 ; i = i.add(threadNumBatchSize)) {
			FindBigPrime fpThread =new FindBigPrime(i, threadNumBatchSize); 
			canInsert = false;
			while(true) {
				if(threadResults.isEmpty() || threadResults.size() < executorInputPoolSize) {
					canInsert = true;
					//System.out.println("empty true");
				}else { 
					for(Future fp : threadResults) {
						if(fp.isDone()) {
							threadResults.remove(fp);
							canInsert = true;
							primeCount++;
							try {
								totalPrimeTime = totalPrimeTime + ((PThreadPerformance)fp.get()).getTimeTaken();
								primeCount = primeCount + ((PThreadPerformance)fp.get()).getPrimeCount();
							} catch (Exception e) { e.printStackTrace(); }
							//System.out.println("remove true");
							break;
						}
					}
				}
				if(canInsert)
					break;
			}
			endTime = System.nanoTime();
			if(primeCount > 0) {
				System.out.println("---\n---\n---\n****Averge time per Prime=" + (((endTime-startTime)/primeCount)/NANO_TO_SECOND_RATIO) + "s" + 
					" \n****Total time taken by all threads combined =" + ((totalPrimeTime)/NANO_TO_SECOND_RATIO) + "s and \n****total prime numbers generated =" + 
					primeCount + "\n****Total time taken by program so far = " + ((endTime-startTime)/NANO_TO_SECOND_RATIO) + "s\n---\n---\n---");
			}
			threadResults.add(executor.submit(fpThread));	
		}
		executor.shutdown();
		try {
			if (!executor.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
				executor.shutdownNow();
			} 
		} catch(Exception e) {
			executor.shutdownNow();
		}
	}
}
class FindBigPrime implements Callable{

	BigInteger inputNumber;
	BigInteger batchSize;
	double startTime, endTime;
	public FindBigPrime(BigInteger inputNumber, BigInteger batchSize) {
		this.inputNumber = new BigInteger(inputNumber.toString()); 
		this.batchSize = batchSize;
	}
	
	@Override
	public PThreadPerformance call() throws Exception {
		boolean prime = true;
		double primeCount = 0;
		double totalPrimeTime = 0;
		BigInteger i = BigInteger.ZERO;
		for(i = new BigInteger(inputNumber.toString()) ; i.compareTo(inputNumber.add(batchSize)) < 0 ; i = i.add(BigInteger.ONE)) {
			prime = true;
			startTime = System.nanoTime();
			for(BigInteger j = BigInteger.TWO ; j.compareTo(i) < 0 ; j = j.add(BigInteger.ONE)) {
				if(i.mod(j).compareTo(BigInteger.ZERO) == 0) {
					prime = false;
					break;
				}
			}
			if(prime) {
				primeCount++;
				endTime = System.nanoTime();
				totalPrimeTime = totalPrimeTime + ((endTime-startTime));
				System.out.println("---Prime Number=" + i +  ", Time taken = " + ((endTime-startTime)/BigPrimeNumberGenerator.NANO_TO_SECOND_RATIO) + "s");
			}
		}
		return new PThreadPerformance(totalPrimeTime, primeCount);
	}
	
}
