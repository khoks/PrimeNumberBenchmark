package com.khoks.test.experiments;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SmartPrimeNumberGenerator implements Runnable {

	private static double START_NUM_RANGE = 1000000000d;
	private static final double END_NUM_RANGE = 100000000000d;
	private static int threadNumBatchSize = 100;
	private static int executorPoolSize = 12;
	private static int executorInputPoolSize = executorPoolSize * 2; 
	public static final double NANO_TO_SECOND_RATIO = 1000000000d;
	public static double PRIME_FUCTION_COUNT = 0;
	public static double LOOP_COUNT = 0;
	
	static ExecutorService EXECUTOR = Executors.newFixedThreadPool(executorPoolSize);
	static double CURRENT_RANGE = 0d ;
	static double START_TIME  = System.nanoTime();
	static double END_TIME = 0d;;
	static Set<Future> THREADS_IN_POOL = new HashSet<Future>();
	static double PRIME_COUNT = 0d;
	static double THREADS_EXECUTED_COUNT = 0d;
	static Integer THREADS_INSERTED_COUNT = 0;
	static double REMAINING_TRIES = executorInputPoolSize;
	static boolean RUNNING_STATE = false;
	
	static SmartPrimeNumberGenerator test1Obj;
	
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println("Generating Prime numbers starting from " + START_NUM_RANGE);
		test1Obj = new SmartPrimeNumberGenerator();		
		test1Obj.threadExecuter();
	}
	
	public void threadExecuter() {
		insertJobs();
	}
	
	public synchronized void startInsertJobs() {
		if(!RUNNING_STATE) {
			insertJobs();
		}
	}
	
	public synchronized void insertJobs() {
		if(RUNNING_STATE)
			return;
		RUNNING_STATE = true;
		boolean canInsert = true;
		FindPrimeSmartly fpThread;
		HashSet<Future> tempThreadsInPoolList = null;
		for(CURRENT_RANGE = START_NUM_RANGE ; CURRENT_RANGE < END_NUM_RANGE ; CURRENT_RANGE = CURRENT_RANGE + threadNumBatchSize) {
			fpThread =new FindPrimeSmartly(CURRENT_RANGE, threadNumBatchSize, this, String.valueOf(THREADS_INSERTED_COUNT));
			canInsert = false;
			while(true) {
				if(THREADS_IN_POOL.isEmpty() || THREADS_IN_POOL.size() < executorInputPoolSize) {
					canInsert = true;
					break;
				}else { 
					/* try {
						Thread.sleep(1000);
					} catch (InterruptedException e) { e.printStackTrace(); } */
					tempThreadsInPoolList = new HashSet<Future>(); 
					for(Future fp : THREADS_IN_POOL) {
						
						if(fp.isDone()) {
							//THREADS_IN_POOL.remove(fp);
							canInsert = true;
							THREADS_EXECUTED_COUNT++;
							try {
								PRIME_COUNT = PRIME_COUNT + ((Double)fp.get());
							} catch (Exception e) { e.printStackTrace(); }
							//break;
						} else {
							tempThreadsInPoolList.add(fp);
						}
					}
					THREADS_IN_POOL = tempThreadsInPoolList;
				}
				if(canInsert)
					break;
			}
			END_TIME = System.nanoTime();
			if((END_TIME-START_TIME)/NANO_TO_SECOND_RATIO > 60) {
				System.out.println();
				System.out.println(((END_TIME-START_TIME)/NANO_TO_SECOND_RATIO)/PRIME_COUNT + 
					" Seconds per Prime after " + (END_TIME-START_TIME)/NANO_TO_SECOND_RATIO + 
					" Seconds. Total Threads executed/inserted - " + THREADS_EXECUTED_COUNT + "/" + THREADS_INSERTED_COUNT);
				//System.out.println((END_TIME-START_TIME)/NANO_TO_SECOND_RATIO + "s");
				//System.out.println("THREADS_INSERTED_COUNT = " + THREADS_INSERTED_COUNT);
				//System.out.println("THREADS_EXECUTED_COUNT = " + THREADS_EXECUTED_COUNT);
				//System.out.println("Prime Count = " + PRIME_COUNT);
			}
			THREADS_IN_POOL.add(EXECUTOR.submit(fpThread));
			THREADS_INSERTED_COUNT++;
			
			if(!THREADS_IN_POOL.isEmpty() && THREADS_IN_POOL.size() == executorInputPoolSize) {
				START_NUM_RANGE = CURRENT_RANGE + threadNumBatchSize;
				RUNNING_STATE = false;
				break;
			}
		}
		START_NUM_RANGE = CURRENT_RANGE + threadNumBatchSize;
		RUNNING_STATE = false;
	}

	void fillThreadPoolOrReapIt() {

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
	String signature = null;
	public FindPrimeSmartly(double inputNumber, int batchSize, SmartPrimeNumberGenerator smartPrimeNumberGenerator, String signature) {
		this.inputNumber = inputNumber; 
		this.batchSize = batchSize;
		this.smartPrimeNumberGenerator = smartPrimeNumberGenerator;
		this.signature = signature;
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
				System.out.print("\033[0;" + signature + "m" + this.signature + ConsoleColors.RESET + " ");
			}
		}
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
