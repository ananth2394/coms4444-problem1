package escape.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class PlayerWrapper {
	private Timer thread;
	private Player player;
	private int id, lastMove;
	private String name;
	private Integer[] playerIds, handleIds;
	private long timeout, originalTimeout;

	public PlayerWrapper(Player player, int id, String name, long timeout) {
		this.player = player;
		this.id = id;
		this.name = name;
		this.timeout = timeout;
		originalTimeout = timeout;
		thread = new Timer();
	}

	public int init(int n) throws Exception {
		Log.record("Initializing player " + id);
		// Initializing ID mapping array
		playerIds = new Integer[n];
		handleIds = new Integer[n];
		for (int i = 0; i < n; ++ i) {
			playerIds[i] = i;
			handleIds[i] = i;
		}
		List<Integer> tempList = Arrays.asList(playerIds);
		Collections.shuffle(tempList);
		playerIds = tempList.toArray(playerIds);

		int zero = -1;
		for (int i = 0; i < n; ++ i) {
			if (playerIds[i] == 0) {
				zero = i;
				break;
			}
		}
		int temp = playerIds[zero];
		playerIds[zero] = playerIds[id];
		playerIds[id] = temp;

		tempList = Arrays.asList(handleIds);
		Collections.shuffle(tempList);
		handleIds = tempList.toArray(handleIds);


		// System.err.print("Player " + (id + 1) + "'s handle numbering:");
		// for (int i = 0; i < n; ++ i)
		// 	System.err.print(" " + handleIds[i]);
		// System.err.println("");
		// System.err.print("Player " + (id + 1) + "'s player numbering:");
		// for (int i = 0; i < n; ++ i)
		// 	System.err.print(" " + playerIds[i]);
		// System.err.println("");

		// Calling player.init(n);
		if (!thread.isAlive()) thread.start();
		thread.call_start(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return player.init(n) - 1;
			}
		});
		int ret = thread.call_wait(timeout);
		long elapsedTime = thread.getElapsedTime();
		timeout -= elapsedTime;
		Log.record("Player " + (id + 1) + "(" + name + ") initialized (" + elapsedTime + "ms)");
		lastMove = handleIds[ret];
		return lastMove;
	}

	public int attempt(List<Integer> conflicts) throws Exception {
		Log.record("Player " + (id + 1) + "(" + name + ") attempting");
		List<Integer> c = new ArrayList<Integer>();
		for (Integer p : conflicts) {
			if (p != id)
				c.add(playerIds[p] + 1);
		}
		// Calling player.attempt(c)
		if (!thread.isAlive()) thread.start();
		thread.call_start(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return player.attempt(c) - 1;
			}
		});
		int ret = thread.call_wait(timeout);
		long elapsedTime = thread.getElapsedTime();
		timeout -= elapsedTime;
		Log.record("Player " + (id + 1) + "(" + name + ") attempts completed (" + elapsedTime + "ms)");
		if (handleIds[ret] == lastMove)
			throw new IllegalArgumentException("Player " + (id + 1) + "(" + name + ") attempts the same handle twice");
		lastMove = handleIds[ret];
		return lastMove;
	}

	public void release() {
		lastMove = -1;
	}

	public long getTotalElapsedTime() {
		return originalTimeout - timeout;
	}

	public String getName() {
		return name;
	}
}
