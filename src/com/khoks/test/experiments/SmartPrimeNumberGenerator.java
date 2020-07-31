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

public class SmartPrimeNumberGenerator {

	private static final double START_NUM_RANGE = 1000000000d;
	private static final double END_NUM_RANGE = 100000000000d;
	private static int threadNumBatchSize = 100;
	private static int executorPoolSize = 24;
	private static int executorInputPoolSize = executorPoolSize * 2; 
	public static final double NANO_TO_SECOND_RATIO = 1000000000d;
	
	public static void main(String[] args) {
		System.out.println("Generating Prime numbers starting from " + START_NUM_RANGE);
		SmartPrimeNumberGenerator test1Obj = new SmartPrimeNumberGenerator();		
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
			FindPrimeSmartly fpThread =new FindPrimeSmartly(i, threadNumBatchSize); 
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
								//totalPrimeTime = totalPrimeTime + ((PThreadPerformance)fp.get()).getTimeTaken();
								//primeCount = primeCount + ((PThreadPerformance)fp.get()).getPrimeCount();
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
			/*if(primeCount > 0) {
				System.out.println("---\n---\n---\n****Averge time per Prime=" + (((endTime-startTime)/primeCount)/NANO_TO_SECOND_RATIO) + "s" + 
					" \n****Total time taken by all threads combined =" + ((totalPrimeTime)/NANO_TO_SECOND_RATIO) + "s and \n****total prime numbers generated =" + 
					primeCount + "\n****Total time taken by program so far = " + ((endTime-startTime)/NANO_TO_SECOND_RATIO) + "s\n---\n---\n---");
			}*/
			if((endTime-startTime)/NANO_TO_SECOND_RATIO > 126) {
				System.out.println((endTime-startTime)/NANO_TO_SECOND_RATIO + "s");
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
class FindPrimeSmartly implements Callable{

	double inputNumber;
	int batchSize;
	int additionLoopCounterDividend = 0;
	int additionLoopCounterDivisor = 2;
	int[] additionLoop = {2,4,2,2};
	double startTime, endTime;
	public FindPrimeSmartly(double inputNumber, int batchSize) {
		this.inputNumber = inputNumber; 
		this.batchSize = batchSize;
	}
	
	@Override
	public PThreadPerformance call() throws Exception {
		boolean prime = true;
		double primeCount = 0;
		double totalPrimeTime = 0;
		double j;
		for(double i = inputNumber + 1 ; i < inputNumber + batchSize ; i = incrementNumberForDivision(i)) {
			prime = true;
			//startTime = System.nanoTime();
			
			j = 3;
			if(i%j == 0) {
				prime = false;
			}
			j = 5;
			if(i%j == 0) {
				prime = false;
			}
			
			if(prime) {
				for(j = 7 ; j < i ; j = incrementDivisorForDivision(j)) {
					if(i%j == 0) {
						prime = false;
						break;
					}
				}
			}
			
			if(prime) {
				//primeCount++;
				//endTime = System.nanoTime();
				//totalPrimeTime = totalPrimeTime + ((endTime-startTime));
				//System.out.println("---Prime Number=" + i +  ", Time taken = " + ((endTime-startTime)/SmartPrimeNumberGenerator.NANO_TO_SECOND_RATIO) + "s");
				System.out.print(".");
			}
		}
		//return new PThreadPerformance(totalPrimeTime, primeCount);
		return null;
	}
	
	
	private double incrementNumberForDivision(double i) {
		
		i += additionLoop[additionLoopCounterDividend];
		
		if(additionLoopCounterDividend == 3) {
			additionLoopCounterDividend = 0;
		} else {
			additionLoopCounterDividend++;			
		}
		
		return i;
	}
	
	private double incrementDivisorForDivision(double i) {
		
		i += additionLoop[additionLoopCounterDivisor];
		
		if(additionLoopCounterDivisor == 3) {
			additionLoopCounterDivisor = 0;
		} else {
			additionLoopCounterDivisor++;			
		}
		
		return i;
	}
}
