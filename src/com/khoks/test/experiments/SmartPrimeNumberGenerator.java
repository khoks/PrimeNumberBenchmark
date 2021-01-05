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

public class SmartPrimeNumberGenerator implements Runnable{

	private static final double START_NUM_RANGE = 1000000000d;
	private static final double END_NUM_RANGE = 100000000000d;
	private static int threadNumBatchSize = 100;
	private static int executorPoolSize = 24;
	private static int executorInputPoolSize = executorPoolSize * 2; 
	public static final double NANO_TO_SECOND_RATIO = 1000000000d;
	public static double PRIME_FUCTION_COUNT = 0;
	public static double LOOP_COUNT = 0;
	
	static ExecutorService EXECUTOR = Executors.newFixedThreadPool(executorPoolSize);
	static double CURRENT_RANGE = 0d ;
	static double START_TIME  = System.nanoTime();
	static double END_TIME = 0d;;
	static Set<Future> THREAD_RESULTS = new HashSet<Future>();
	static double PRIME_COUNT = 0d;
	static double REMAINING_TRIES = executorInputPoolSize;
	static boolean RUNNING_STATE = false;
	
	static SmartPrimeNumberGenerator test1Obj;
	
	
	public static void main(String[] args) {
		System.out.println("Generating Prime numbers starting from " + START_NUM_RANGE);
		test1Obj = new SmartPrimeNumberGenerator();		
		test1Obj.threadExecuter();
	}
	
	public void threadExecuter() {
		double totalPrimeTime = 0d;
		insertJobs();
	}
	
	public synchronized void startInsertJobs() {

		/*
		 * while(RUNNING_STATE) { //SmartPrimeNumberGenerator.incrementLoopCount(); try
		 * { Thread.sleep(2); } catch (InterruptedException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } }
		 */
		insertJobs();
	}
	
	public synchronized void insertJobs() {
		RUNNING_STATE = true;
		boolean canInsert = true;
		FindPrimeSmartly fpThread;
		for(CURRENT_RANGE = START_NUM_RANGE ; CURRENT_RANGE < END_NUM_RANGE ; CURRENT_RANGE = CURRENT_RANGE + threadNumBatchSize) {
			//SmartPrimeNumberGenerator.incrementLoopCount();
			fpThread =new FindPrimeSmartly(CURRENT_RANGE, threadNumBatchSize, this); 
			canInsert = false;
			while(true) {
				//SmartPrimeNumberGenerator.incrementLoopCount();
				if(THREAD_RESULTS.isEmpty() || THREAD_RESULTS.size() < executorInputPoolSize) {
					canInsert = true;
					//System.out.println("empty true");
				}else { 
					for(Future fp : THREAD_RESULTS) {
						//below line to be removed
						//SmartPrimeNumberGenerator.incrementLoopCount();
						
						if(fp.isDone()) {
							THREAD_RESULTS.remove(fp);
							canInsert = true;
							PRIME_COUNT++;
							try {
								PRIME_COUNT = PRIME_COUNT + ((Double)fp.get());
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
			END_TIME = System.nanoTime();
			/*if(primeCount > 0) {
				System.out.println("---\n---\n---\n****Averge time per Prime=" + (((endTime-startTime)/primeCount)/NANO_TO_SECOND_RATIO) + "s" + 
					" \n****Total time taken by all threads combined =" + ((totalPrimeTime)/NANO_TO_SECOND_RATIO) + "s and \n****total prime numbers generated =" + 
					primeCount + "\n****Total time taken by program so far = " + ((endTime-startTime)/NANO_TO_SECOND_RATIO) + "s\n---\n---\n---");
			}*/
			if((END_TIME-START_TIME)/NANO_TO_SECOND_RATIO > 60) {
				System.out.println((END_TIME-START_TIME)/NANO_TO_SECOND_RATIO + "s");
				System.out.println("PRIME_FUCTION_COUNT = " + PRIME_FUCTION_COUNT); 
				System.out.println("LOOP_COUNT = " + LOOP_COUNT);
				System.out.println("Prime Count = " + PRIME_COUNT);
			}
			THREAD_RESULTS.add(EXECUTOR.submit(fpThread));
			
			if(!THREAD_RESULTS.isEmpty() && THREAD_RESULTS.size() == executorInputPoolSize) {
				break;
			}
		}
		RUNNING_STATE = false;
	}
	
	public static synchronized void incrementPrimeFunctionCount() {
		PRIME_FUCTION_COUNT++;
	}
	
	public static synchronized void incrementLoopCount() {
		LOOP_COUNT++;
	}

	@Override
	public void run() {
		startInsertJobs();
		
	}
}
class FindPrimeSmartly implements Callable{

	double inputNumber;
	int batchSize;
	SmartPrimeNumberGenerator smartPrimeNumberGenerator;
	int additionLoopCounterDividend = 0;
	int additionLoopCounterDivisor = 2;
	int[] additionLoop = {2,4,2,2};
	double startTime, endTime;
	public FindPrimeSmartly(double inputNumber, int batchSize, SmartPrimeNumberGenerator smartPrimeNumberGenerator) {
		this.inputNumber = inputNumber; 
		this.batchSize = batchSize;
		this.smartPrimeNumberGenerator = smartPrimeNumberGenerator;
	}
	
	@Override
	public Double call() throws Exception {
		boolean prime = true;
		double primeCount = 0;
		double totalPrimeTime = 0;
		double j;
		for(double i = inputNumber + 1 ; i < inputNumber + batchSize ; i = incrementNumberForDivision(i)) {
			//below line to be removed
			//SmartPrimeNumberGenerator.incrementPrimeFunctionCount();
			
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
				primeCount++;
				//endTime = System.nanoTime();
				//totalPrimeTime = totalPrimeTime + ((endTime-startTime));
				//System.out.println("---Prime Number=" + i +  ", Time taken = " + ((endTime-startTime)/SmartPrimeNumberGenerator.NANO_TO_SECOND_RATIO) + "s");
				System.out.print(".");
			}
		}
		//return new PThreadPerformance(totalPrimeTime, primeCount);
		Thread t = new Thread(smartPrimeNumberGenerator);
		t.start();
		return primeCount;
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
