
package app;

import java.util.*;

public class Main {
    public static void main (String[] args) {
        if ((null == args) || (2 > args.length)) {
            args = new String[] { "a use case", "a user" };
        }
        new UseCase(args[0]).run(new User(args[1]));
    } 
}

interface Action {
    void doAction(User user);
}

class User {
    String name;
    User(String name) {
        this.name = name;
    }
    public String toString() { return name; }
}

class UseCase implements Runnable {
    String name;
    User user;
    List actions;

    UseCase(String name) {
        this.name = name;
        this.actions = new ArrayList();
    }
    public void setUser(User user) {
        this.user = user;
    }
    public void run() {
        run(user);
    }
    public void run(User user) {
        List curActions = loadActions();
        for (Iterator iter = curActions.iterator(); iter.hasNext();) {
            ((Action) iter.next()).doAction(user);
        } 
    }
    public void addAction(Action action) {
        if (null != action) {
            actions.add(action);
        }
    }

    public String toString() { return name; }

    protected List loadActions() {
        return actions;
    }

}


