aspect Gets {
    pointcut setters(): call(void *.set(..));
    pointcut getters(): call(Object *.get());

    pointcut all(): setters() || getters();
    before(): all() {}
    after(): all() {}
}
