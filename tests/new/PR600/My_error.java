aspect My_error {

    interface Queue {}
    Queue Queue.next = null; 

    public void Queue.doIt() {
        if (next == null) {
            System.out.println("End of queue reached");
        } else {
            System.out.println("\tCall received  by: "+this.getClass().getName());
            System.out.println("\tCall forwarded to: "+next.getClass().getName());
            next.doIt();
        }
    }

    public void Queue.setNext(Queue next) {
        this.next = next;
    }

    declare parents: A implements Queue;
    declare parents: B implements Queue;
    declare parents: C implements Queue;  

    // This is the problematic declaration. If removed, the program works fine.
    // If replaced by an around advice, the program also works fine.

    public void C.doIt() {
        System.out.println("Hurray! The call has been received by C!");
    }
}
