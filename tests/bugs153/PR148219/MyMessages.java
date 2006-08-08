public aspect MyMessages {
    pointcut getResourceString(String key): args(key, ..) &&
    call (* *.getResourceString(String, ..));

    String around(String key):getResourceString(key) {
        return key;
    }
}