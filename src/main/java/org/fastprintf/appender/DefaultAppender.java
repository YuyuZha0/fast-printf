package org.fastprintf.appender;

import org.fastprintf.Flag;
import org.fastprintf.FormatContext;
import org.fastprintf.Specifier;
import org.fastprintf.seq.Seq;
import org.fastprintf.traits.FormatTraits;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class DefaultAppender implements Appender {

  private final Specifier specifier;
  private final FormatContext context;

  public DefaultAppender(Specifier specifier, FormatContext context) {
    this.specifier = Objects.requireNonNull(specifier, "specifier");
    this.context = Objects.requireNonNull(context, "context");
  }

  private static Seq justify(EnumSet<Flag> flags, int width, Seq seq) {
    int length = seq.length();
    if (length >= width) {
      return seq;
    }
    char pad = flags.contains(Flag.ZERO_PAD) ? '0' : ' ';
    if (flags.contains(Flag.LEFT_JUSTIFY)) {
      return seq.append(Seq.repeated(pad, width - length));
    }
    return seq.prepend(Seq.repeated(pad, width - length));
  }

  private int nextInt(Iterator<FormatTraits> iterator) {
    if (!iterator.hasNext()) {
      throw new NoSuchElementException("Missing argument for specifier: " + specifier);
    }
    return iterator.next().asInt();
  }

  @Override
  public void append(List<Seq> collect, Iterator<FormatTraits> traitsIterator) {
    if (context.isPrecedingWidth()) {
      context.setWidth(nextInt(traitsIterator));
    }
    if (context.isPrecedingPrecision()) {
      context.setPrecision(nextInt(traitsIterator));
    }
    if (!traitsIterator.hasNext()) {
      throw new NoSuchElementException("Missing argument for specifier: " + specifier);
    }
    FormatTraits traits = traitsIterator.next();
    Seq seq = traits.seqForSpecifier(specifier, context);
    collect.add(justify(context.getFlags(), context.getWidth(), seq));
  }

  public Specifier getSpecifier() {
    return specifier;
  }

  public FormatContext getContext() {
    return context;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append('%');
    for (Flag flag : context.getFlags()) {
      switch (flag) {
        case ALTERNATE:
          builder.append('#');
          break;
        case PLUS:
          builder.append('+');
          break;
        case LEFT_JUSTIFY:
          builder.append('-');
          break;
        case ZERO_PAD:
          builder.append('0');
          break;
        case LEADING_SPACE:
          builder.append(' ');
          break;
      }
    }
    int width = context.getWidth();
    if (width == FormatContext.PRECEDING) {
      builder.append('*');
    } else if (width != FormatContext.UNSET) {
      builder.append(width);
    }
    int precision = context.getPrecision();
    if (precision == FormatContext.PRECEDING) {
      builder.append(".*");
    } else if (precision != FormatContext.UNSET) {
      builder.append('.').append(precision);
    }
    builder.append(specifier);
    return builder.toString();
  }
}
