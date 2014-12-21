package com.outjected.unique;

import java.util.Date;

public class UniqueLong {

	private final long epoch;

	private static final int SIGN_BITS = 1;
	private static final int TIME_BITS = 41;
	private static final int DATACENTER_BITS = 4;
	private static final int SERVER_BITS = 6;
	private static final int SEQUENCE_BITS = 12;

	private static final long MIN_SHIFTED_TIMESTAMP = (long) (Math.pow(2, 22) - 1);
	private static final long MAX_SHIFTED_TIMESTAMP = (long) Math.pow(2, 64) - 1;
	private static final long MAX_SEQUENCE = (long) Math.pow(2, SEQUENCE_BITS) - 1;

	private static final int TIME_SHIFT = SEQUENCE_BITS + SERVER_BITS
			+ DATACENTER_BITS;
	private final long workerPart;

	private long sequence = 0;
	private long lastShiftedTimestamp = 0;

	/**
	 * Creates a new generator using a custom epoch and datacenter/server
	 * identifiers
	 * 
	 * @param epoch
	 * @param dataCenter
	 * @param server
	 */
	public UniqueLong(long epoch, int dataCenter, int server) {
		this.epoch = epoch;
		workerPart = createWorkerPart(dataCenter, server);

	}

	/**
	 * Generates a unique signed long
	 * 
	 * @return long
	 */
	public synchronized long generate() {

		long shiftedTimestamp = getCurrentShiftedTimestamp();

		while (lastShiftedTimestamp > shiftedTimestamp) {
			try {
				// Clock is running backwards or was incremented because of
				// exhaustion. Sleep 1 ms and try again
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			shiftedTimestamp = (System.currentTimeMillis() - epoch) << TIME_SHIFT;
		}

		if (shiftedTimestamp < MIN_SHIFTED_TIMESTAMP
				|| shiftedTimestamp > MAX_SHIFTED_TIMESTAMP) {
			// The current time cannot be less than the EPOCH
			throw new RuntimeException("Invalid System Clock was "
					+ new Date(System.currentTimeMillis()));
		}

		if (lastShiftedTimestamp < shiftedTimestamp) {
			// Timestamp has advanced so reset the incrementing long
			lastShiftedTimestamp = shiftedTimestamp;
			sequence = 0L;
		}

		final long numberPart = nextNumberPart();
		if (numberPart == -1) {
			// We have exhausted all possible for this ms so increment and
			// restart
			lastShiftedTimestamp++;
			return generate();
		}

		return shiftedTimestamp | workerPart | numberPart;
	}

	private long nextNumberPart() {
		long result = sequence++;
		return result <= MAX_SEQUENCE ? result : -1;
	}

	private long getCurrentShiftedTimestamp() {
		return (System.currentTimeMillis() - epoch) << TIME_SHIFT;
	}

	public static long createWorkerPart(int dataCenter, int server) {
		long total_bits = SIGN_BITS + TIME_BITS + DATACENTER_BITS + SERVER_BITS
				+ SEQUENCE_BITS;
		final long maxDataCenter = (long) (Math.pow(2, DATACENTER_BITS) - 1);
		final long maxServer = (long) (Math.pow(2, SERVER_BITS) - 1);

		if (total_bits != 64) {
			throw new IllegalArgumentException(
					"Cannot be more that 64 bits assigned. Was " + total_bits);
		}

		if (server < 0 || server > Math.pow(2, SERVER_BITS) - 1) {
			throw new IllegalArgumentException(String.format(
					"Server-ID must be between 0 and %s. Was %s", maxServer,
					server));
		} else if (dataCenter < 0 || dataCenter > maxDataCenter) {
			throw new IllegalArgumentException(String.format(
					"DataCenter-ID must be between 0 and %s. Was %s",
					maxDataCenter, dataCenter));
		}

		final long shiftedDataCenter = dataCenter << SEQUENCE_BITS
				+ SERVER_BITS;
		final long shiftedServer = server << SEQUENCE_BITS;
		return shiftedDataCenter | shiftedServer;
	}
}
