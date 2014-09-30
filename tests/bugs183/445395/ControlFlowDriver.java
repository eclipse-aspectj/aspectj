//package org.acmsl.pocs.lambdafor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ControlFlowDriver {

	private static boolean m__bUsed = false;

	public ControlFlowDriver() {
	}

	protected static void immutableSetUsed(final boolean used) {
		m__bUsed = used;
	}

	protected static void setUsed(final boolean used) {
		immutableSetUsed(used);
	}

	public static boolean isUsed() {
		return m__bUsed;
	}

	public <C extends Collection<I>, I, R> Collection<R> forloop(final C items,
			final Function<I, R> lambda) {
		setUsed(true);

		final List<R> result = new ArrayList<R>(items.size());

		final List<I> list = new ArrayList<I>(items);

		int position = -1;

		while (true) {
			ControlFlowCommand command = waitForCommand();

			switch (command) {
			case NEXT:
				position++;
				break;
			case PREVIOUS:
				position++;
				break;
			case RELOAD:
				break;
			default:
				break;
			}

			if (position < 0) {
				position = 0;
			} else if (position > list.size() - 1) {
				break;
			}

			result.set(position, lambda.apply(list.get(position)));
		}

		return result;
	}

	protected ControlFlowCommand waitForCommand() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException interruptedException) {
			// whatever
		}

		return ControlFlowCommand.NEXT;
	}

}
