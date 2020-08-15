/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Noel Markham, Matthew Webster     initial implementation 
 * ******************************************************************/

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Noel Markham
 */
public class RunWeaveTests {

	private static boolean RUN_SMALL = true;
	private static boolean RUN_MED = true;
	private static boolean RUN_LARGE = true;
		
	private static final String SMALL_PROGRAM_ARGS = "-c 8 -m 100 -l 10 "; // must end on a space in case any more arguments are added later in the script
	private static final String MEDIUM_PROGRAM_ARGS = "-c 64 -m 100 -l 10 ";
	private static final String LARGE_PROGRAM_ARGS = "-c 512 -m 100 -l 10 ";
	
	private static final String WARMUP_ARGS = "-c 3 -m 100 -l 10 ";

	private static final int ITERATIONS = 3;
	
	private static String filename;
		
	private static long[][] times = new long[7][3 * ITERATIONS];
		
	public static void main(String[] args) throws IOException {
		
		parseArgs(args);
		
		// Purely as a warm-up...
		System.out.println("Warming up...");
		WeaveTest.main(split(WARMUP_ARGS + "-i " + ITERATIONS));

		if (RUN_SMALL) {
			WeaveTest.main(
				split(SMALL_PROGRAM_ARGS + "-echo -i " + ITERATIONS));

			System.arraycopy(WeaveTest.compileTimes, 0, times[0], 0, ITERATIONS);

			System.arraycopy(WeaveTest.executionFastTimes, 0, times[1], 0, ITERATIONS);
			System.arraycopy(WeaveTest.executionMedTimes, 0, times[2], 0, ITERATIONS);
			System.arraycopy(WeaveTest.executionSlowTimes, 0, times[3], 0, ITERATIONS);

			System.arraycopy(WeaveTest.getFastTimes, 0, times[4], 0, ITERATIONS);
			System.arraycopy(WeaveTest.getMedTimes, 0, times[5], 0, ITERATIONS);
			System.arraycopy(WeaveTest.getSlowTimes, 0, times[6], 0, ITERATIONS);
		}		
		
		if (RUN_MED) {
			WeaveTest.main(
				split(MEDIUM_PROGRAM_ARGS + "-echo -i " + ITERATIONS));

			System.arraycopy(WeaveTest.compileTimes, 0, times[0], ITERATIONS, 2 * ITERATIONS - 3);

			System.arraycopy(WeaveTest.executionFastTimes, 0, times[1], ITERATIONS, 2 * ITERATIONS - 3);
			System.arraycopy(WeaveTest.executionMedTimes, 0, times[2], ITERATIONS, 2 * ITERATIONS - 3);
			System.arraycopy(WeaveTest.executionSlowTimes, 0, times[3], ITERATIONS, 2 * ITERATIONS - 3);

			System.arraycopy(WeaveTest.getFastTimes, 0, times[4], ITERATIONS, 2 * ITERATIONS - 3);
			System.arraycopy(WeaveTest.getMedTimes, 0, times[5], ITERATIONS, 2 * ITERATIONS - 3);
			System.arraycopy(WeaveTest.getSlowTimes, 0, times[6], ITERATIONS, 2 * ITERATIONS - 3);
			
		}		
		
		if (RUN_LARGE) {
			WeaveTest.main(
				split(LARGE_PROGRAM_ARGS + "-echo -i " + ITERATIONS));

			System.arraycopy(WeaveTest.compileTimes, 0, times[0], 2 * ITERATIONS, 3 * ITERATIONS - 6);

			System.arraycopy(WeaveTest.executionFastTimes, 0, times[1], 2 * ITERATIONS, 3 * ITERATIONS - 6);
			System.arraycopy(WeaveTest.executionMedTimes, 0, times[2], 2 * ITERATIONS, 3 * ITERATIONS - 6);
			System.arraycopy(WeaveTest.executionSlowTimes, 0, times[3], 2 * ITERATIONS, 3 * ITERATIONS - 6);

			System.arraycopy(WeaveTest.getFastTimes, 0, times[4], 2 * ITERATIONS, 3 * ITERATIONS - 6);
			System.arraycopy(WeaveTest.getMedTimes, 0, times[5], 2 * ITERATIONS, 3 * ITERATIONS - 6);
			System.arraycopy(WeaveTest.getSlowTimes, 0, times[6], 2 * ITERATIONS, 3 * ITERATIONS - 6);
			
		}		

		createCSV();
	}
	
	/* Compatibility with JDK 1.3.1 */
	public static String[] split (String s) {
		List list = new ArrayList();
		StringTokenizer st = new StringTokenizer(s," ");
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}
		
		String[] result = new String[list.size()];
		list.toArray(result);
		return result;
	}
	
	private static void parseArgs(String args[]) {
		
		if(args == null || args.length <= 0) {
			System.out.println("Running all program tests");
			return;
		}
			
		if(args[0].equals("-small")) {
			System.out.println("Running small-sized program test only");
			RUN_SMALL = true;
			RUN_MED = false;
			RUN_LARGE = false;
		}
		else if(args[0].equals("-medium")) {
			System.out.println("Running small- and medium -sized program tests");
			RUN_SMALL = true;
			RUN_MED = true;
			RUN_LARGE = false;
		}
		else if(args[0].equals("-large")) {
			System.out.println("Running all program tests");
			RUN_SMALL = true;
			RUN_MED = true;
			RUN_LARGE = true;
		}
		else usage();
	}
	
	private static void usage() {
		System.err.println("Usage:");
		System.err.println("\tjava RunWeaveTests [-small|-medium|-large]");
		System.exit(-1);
	}
	
	private static void createCSV() {
		String NL = System.getProperty("line.separator", "\n");
		StringBuffer csv = new StringBuffer(1000);
		 
		csv.append("Test Results");
		
		if(RUN_SMALL) for(int i = 0; i < ITERATIONS; i++) csv.append("," + SMALL_PROGRAM_ARGS);
		if(RUN_MED) for(int i = 0; i < ITERATIONS; i++) csv.append("," + MEDIUM_PROGRAM_ARGS);
		if(RUN_LARGE) for(int i = 0; i < ITERATIONS; i++) csv.append("," + LARGE_PROGRAM_ARGS);
		
		csv.append(NL);
		csv.append("Compile");
		if(RUN_SMALL) for(int i = 0; i < ITERATIONS; i++) csv.append("," + times[0][i]);
		if(RUN_MED) for(int i = ITERATIONS; i < (2 * ITERATIONS); i++) csv.append("," + times[0][i]);
		if(RUN_LARGE) for(int i = (2 * ITERATIONS); i < (3 * ITERATIONS); i++) csv.append("," + times[0][i]);

		csv.append(NL);

		csv.append(WeaveTest.EXECUTION_FAST);
		if(RUN_SMALL) for(int i = 0; i < ITERATIONS; i++) csv.append("," + times[1][i]);
		if(RUN_MED) for(int i = ITERATIONS; i < (2 * ITERATIONS); i++) csv.append("," + times[1][i]);
		if(RUN_LARGE) for(int i = (2 * ITERATIONS); i < (3 * ITERATIONS); i++) csv.append("," + times[1][i]);
		csv.append(NL);
		
		csv.append(WeaveTest.EXECUTION_MED);
		if(RUN_SMALL) for(int i = 0; i < ITERATIONS; i++) csv.append("," + times[2][i]);
		if(RUN_MED) for(int i = ITERATIONS; i < (2 * ITERATIONS); i++) csv.append("," + times[2][i]);
		if(RUN_LARGE) for(int i = (2 * ITERATIONS); i < (3 * ITERATIONS); i++) csv.append("," + times[2][i]);
		csv.append(NL);

		csv.append(WeaveTest.EXECUTION_SLOW);
		if(RUN_SMALL) for(int i = 0; i < ITERATIONS; i++) csv.append("," + times[3][i]);
		if(RUN_MED) for(int i = ITERATIONS; i < (2 * ITERATIONS); i++) csv.append("," + times[3][i]);
		if(RUN_LARGE) for(int i = (2 * ITERATIONS); i < (3 * ITERATIONS); i++) csv.append("," + times[3][i]);
		csv.append(NL);

		csv.append(WeaveTest.GET_FAST);
		if(RUN_SMALL) for(int i = 0; i < ITERATIONS; i++) csv.append("," + times[4][i]);
		if(RUN_MED) for(int i = ITERATIONS; i < (2 * ITERATIONS); i++) csv.append("," + times[4][i]);
		if(RUN_LARGE) for(int i = (2 * ITERATIONS); i < (3 * ITERATIONS); i++) csv.append("," + times[4][i]);
		csv.append(NL);

		csv.append(WeaveTest.GET_MED);
		if(RUN_SMALL) for(int i = 0; i < ITERATIONS; i++) csv.append("," + times[5][i]);
		if(RUN_MED) for(int i = ITERATIONS; i < (2 * ITERATIONS); i++) csv.append("," + times[5][i]);
		if(RUN_LARGE) for(int i = (2 * ITERATIONS); i < (3 * ITERATIONS); i++) csv.append("," + times[5][i]);
		csv.append(NL);

		csv.append(WeaveTest.GET_SLOW);
		if(RUN_SMALL) for(int i = 0; i < ITERATIONS; i++) csv.append("," + times[6][i]);
		if(RUN_MED) for(int i = ITERATIONS; i < (2 * ITERATIONS); i++) csv.append("," + times[6][i]);
		if(RUN_LARGE) for(int i = (2 * ITERATIONS); i < (3 * ITERATIONS); i++) csv.append("," + times[6][i]);
		csv.append(NL);
		
		filename = createFilename();
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			bos.write(new String(csv).getBytes());
			
			bos.close();
			fos.close();
		} catch (IOException e) {
			System.err.println("Could not print to file " + filename);
			System.err.println(csv);
		}
		System.out.println("Written: " + filename);
	}
	
	private static String createFilename() {
		
		return (getVMInfo() + "_" + getDateAndTime()).replace('.', '_') + ".csv";
	}
	
	private static StringBuffer getDateAndTime() {
		Calendar c = Calendar.getInstance();
		
		StringBuffer sb = new StringBuffer(15);
		sb.append(c.get(Calendar.YEAR));
		int month = (c.get(Calendar.MONTH)) + 1;
		if(month < 10) sb.append(0);		
		sb.append(month);
		int date = c.get(Calendar.DAY_OF_MONTH);
		if(date < 10) sb.append(0);
		sb.append(date);
		
		sb.append("_");
		
		int hour = c.get(Calendar.HOUR_OF_DAY);
		if(hour < 10) sb.append(0);
		sb.append(hour);
		int minute = c.get(Calendar.MINUTE);	
		if(minute < 10) sb.append(0);
		sb.append(minute);
		int second = c.get(Calendar.SECOND);
		if(second < 10) sb.append(0);
		sb.append(second);
		
		
		return sb;
	}
	
	private static StringBuffer getVMInfo() {
		StringBuffer sb = new StringBuffer(40);
		
		String vm = System.getProperty("java.vm.vendor");
		
		if(vm.equals("IBM Corporation")) {
			String vminfo = System.getProperty("java.vm.info");
			String[] vminfoComponents = split(vminfo);
			
			sb.append(vminfoComponents[2] + ("_"));
			sb.append(vminfoComponents[3] + ("_"));
			sb.append(vminfoComponents[4] + ("_"));
			sb.append(vminfoComponents[6]);
		}
		else if(vm.equals("Sun Microsystems Inc.")) {
			String vminfo = System.getProperty("java.vm.name");
			String[] vminfoComponents = split(vminfo);

			sb.append("Sun_");
			sb.append(System.getProperty("java.vm.version") + "_");
			sb.append(vminfoComponents[2]);
		}

		return sb;
	}
}
