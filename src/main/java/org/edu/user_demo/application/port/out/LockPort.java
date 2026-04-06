package org.edu.user_demo.application.port.out;

public interface LockPort {

    boolean tryLock(String key);

    void unlock(String key);
}
