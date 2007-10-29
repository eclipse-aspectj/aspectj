interface I {
        interface J< T > {
                T getT();
        }
}
public aspect ExampleG {
        public T I.J< T >.intro() {
                return null;
        }
}