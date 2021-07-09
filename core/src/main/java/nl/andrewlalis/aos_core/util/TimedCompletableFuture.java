package nl.andrewlalis.aos_core.util;

import java.util.concurrent.CompletableFuture;

/**
 * A completable future which keeps track of how long it's been since it was
 * initialized.
 * @param <T> The type which is produced when this completes.
 */
public class TimedCompletableFuture<T> extends CompletableFuture<T> {
	private final long creationTimestamp;

	public TimedCompletableFuture(long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public TimedCompletableFuture() {
		this(System.currentTimeMillis());
	}

	public long getElapsedMillis() {
		return System.currentTimeMillis() - this.creationTimestamp;
	}
}
