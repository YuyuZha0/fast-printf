module io.fastprintf {
  requires java.base;

  // The Main Entry point (FastPrintf, Args, etc.)
  exports io.fastprintf;

  // REQUIRED: Because 'Args' extends 'Iterable<FormatTraits>',
  // this package must be visible to consumers.
  exports io.fastprintf.traits;

// HIDDEN PACKAGES (Internal details):
// io.fastprintf.seq        <- Users don't need to touch the Rope implementation
// io.fastprintf.appender   <- Users use FastPrintf, they don't manually call Appenders
// io.fastprintf.number     <- Internal Schubfach wrappers
// io.fastprintf.util       <- Internal utilities
}
