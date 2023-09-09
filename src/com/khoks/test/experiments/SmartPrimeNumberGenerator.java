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
	// private static int executorPoolSize = 12;
	// private static int executorInputPoolSize = executorPoolSize * 2;
	private static int executorInputPoolSize = 32;
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
				System.out+.print("\033[0;"  signature + "m" + this.signature + ConsoleColors.RESET + " ");
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
class ConsoleColors {
    // Reset
    public static final String RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE

    // Bold
    public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    // Underline
    public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
    public static final String RED_UNDERLINED = "\033[4;31m";    // RED
    public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
    public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
    public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
    public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

    // Background
    public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
    public static final String RED_BACKGROUND = "\033[41m";    // RED
    public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
    public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
    public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
    public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

    // High Intensity
    public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
    public static final String RED_BRIGHT = "\033[0;91m";    // RED
    public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
    public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
    public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
    public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
    public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
    public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

    // Bold High Intensity
    public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
    public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
    public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
    public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
    public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

    // High Intensity backgrounds
    public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
    public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
    public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
    public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
    public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
    public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
    public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
    public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE
}
