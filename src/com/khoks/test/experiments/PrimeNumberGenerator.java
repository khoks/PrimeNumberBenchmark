package com.khoks.test.experiments;

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

public class PrimeNumberGenerator {

	private static final double START_NUM_RANGE = 100000000d;
	private static final double END_NUM_RANGE = 10000000000d;
	private static int threadNumBatchSize = 100;
	private static int executorPoolSize = 24;
	private static int executorInputPoolSize = executorPoolSize * 2; 
	public static final double NANO_TO_SECOND_RATIO = 1000000000d;
	
	public static void main(String[] args) {
		System.out.println("Generating Prime numbers starting from " + START_NUM_RANGE);
		PrimeNumberGenerator test1Obj = new PrimeNumberGenerator();		
		test1Obj.threadExecuter();
	}
	
	public void threadExecuter() {
		ExecutorService executor = Executors.newFixedThreadPool(executorPoolSize);
		
		double i = 0d ;
		double startTime  = System.nanoTime(), endTime;
		Set<Future> threadResults = new HashSet<Future>();
		boolean canInsert = true;
		double primeCount = 0d;
		double totalPrimeTime = 0d;
		
		for(i = START_NUM_RANGE ; i < END_NUM_RANGE ; i = i + threadNumBatchSize) {
			FindPrime fpThread =new FindPrime(i, threadNumBatchSize); 
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
class FindPrime implements Callable{

	double inputNumber;
	int batchSize;
	double startTime, endTime;
	public FindPrime(double inputNumber, int batchSize) {
		this.inputNumber = inputNumber; 
		this.batchSize = batchSize;
	}
	
	@Override
	public PThreadPerformance call() throws Exception {
		boolean prime = true;
		double primeCount = 0;
		double totalPrimeTime = 0;
		for(double i = inputNumber ; i < inputNumber + batchSize ; i++) {
			prime = true;
			startTime = System.nanoTime();
			for(double j = 2 ; j < i ; j++) {
				if(i%j == 0) {
					prime = false;
					break;
				}
			}
			if(prime) {
				primeCount++;
				endTime = System.nanoTime();
				totalPrimeTime = totalPrimeTime + ((endTime-startTime));
				System.out.println("---Prime Number=" + i +  ", Time taken = " + ((endTime-startTime)/PrimeNumberGenerator.NANO_TO_SECOND_RATIO) + "s");
			}
		}
		return new PThreadPerformance(totalPrimeTime, primeCount);
	}
	
}
class PThreadPerformance {
	double primeCount = 0d;
	double timeTaken = 0d;
	public PThreadPerformance(double timeTaken, double primeCount) {
		this.primeCount = primeCount;
		this.timeTaken = timeTaken;
	}
	public double getPrimeCount() {
		return primeCount;
	}
	public void setPrimeCount(double primeCount) {
		this.primeCount = primeCount;
	}
	public double getTimeTaken() {
		return timeTaken;
	}
	public void setTimeTaken(double timeTaken) {
		this.timeTaken = timeTaken;
	}
}
