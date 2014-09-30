//package org.acmsl.pocs.lambdafor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ControlFlow
{
    public <C extends Collection<I>, I, R> Collection<R> forloop( final C items, final Function<I, R> lambda)
    {
        return functionalForLoop(items, lambda);
    }

    public <C extends Collection<I>, I, R> Collection<R> functionalForLoop( final C items,  final Function<I, R> lambda)
    {
        return items.stream().map(lambda::apply).collect(Collectors.toList());
    }

    
    public Collection iterativeForloop( final Collection items,  final Function lambda)
    {
         final List<Object> result = new ArrayList<>();

        for (final Object item: items)
        {
            result.add(lambda.<Object>apply(item));
        }

        return result;
    }

    public <C extends Collection<I>, I, R> Collection<R> externallyDrivenForloop(
         final ControlFlowDriver driver,  final C items,  final Function<I, R> lambda)
    {
         final List<R> result = new ArrayList<>(items.size());

         final List<I> list = new ArrayList<>(items);

        int position = -1;

        while (true)
        {
            ControlFlowCommand command = driver.waitForCommand();

            switch (command)
            {
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

            if (position < 0)
            {
                position = 0;
            }
            else if (position > list.size() - 1)
            {
                break;
            }

            result.set(position, lambda.apply(list.get(position)));
        }

        return result;
    }
}
