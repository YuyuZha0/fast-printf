package org.fastprintf.util;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Cleaner extends PhantomReference<Object> {

  private static final ReferenceQueue<Object> QUEUE = new ReferenceQueue<>();
  private static final Set<Cleaner> REFERENCES = Collections.newSetFromMap(new IdentityHashMap<>());
  private static final AtomicBoolean CLEANER_THREAD_STARTED = new AtomicBoolean(false);
  private static final Thread CLEANER_THREAD;

  static {
    CLEANER_THREAD =
        new Thread(
            () -> {
              while (true) {
                try {
                  Cleaner cleaner = (Cleaner) QUEUE.remove();
                  cleaner.clean();
                } catch (InterruptedException e) {
                  // ignore
                }
              }
            },
            "FastPrintfCleanerThread");
    CLEANER_THREAD.setDaemon(true);
  }

  private final Runnable thunk;

  private Cleaner(Object referent, Runnable thunk) {
    super(referent, QUEUE);
    this.thunk = thunk;
  }

  public static Cleaner create(Object referent, Runnable thunk) {
    Objects.requireNonNull(referent, "referent");
    Objects.requireNonNull(thunk, "thunk");
    Cleaner cleaner = new Cleaner(referent, thunk);
    synchronized (REFERENCES) {
      REFERENCES.add(cleaner);
    }
    REFERENCES.add(cleaner);
    if (CLEANER_THREAD_STARTED.compareAndSet(false, true)) {
      CLEANER_THREAD.start();
    }
    return cleaner;
  }

  public void clean() {
    boolean removed;
    synchronized (REFERENCES) {
      removed = REFERENCES.remove(this);
    }
    if (removed) {
      thunk.run();
    }
  }
}
