package org.fastprintf.appender;

import org.fastprintf.Flag;
import org.fastprintf.FormatContext;
import org.fastprintf.Specifier;
import org.fastprintf.seq.Seq;
import org.fastprintf.traits.FormatTraits;

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

  private int nextInt(Iterator<FormatTraits> iterator) {
    if (!iterator.hasNext()) {
      throw new NoSuchElementException("Missing argument for specifier: " + specifier);
    }
    return iterator.next().asInt();
  }

  @Override
  public void append(List<Seq> collect, Iterator<FormatTraits> traitsIterator) {
    if (context.isPrecedingWidth()) {
      int w = nextInt(traitsIterator);
      if (w >= 0) {
        context.setWidth(w);
      } else {
        context.addFlag(Flag.LEFT_JUSTIFY);
        context.setWidth(-w);
      }
    }
    if (context.isPrecedingPrecision()) {
      int p = nextInt(traitsIterator);
      if (p >= 0) {
        context.setPrecision(p);
      } else {
        context.setPrecision(FormatContext.UNSET);
      }
    }
    if (!traitsIterator.hasNext()) {
      throw new NoSuchElementException("Missing argument for specifier: " + specifier);
    }
    FormatTraits traits = traitsIterator.next();
    collect.add(format(traits));
  }

  private Seq format(FormatTraits traits) {
    if (traits.isNull()) {
      return SeqFormatter.forNull(context);
    }
    switch (specifier) {
      case SIGNED_DECIMAL_INTEGER:
        return SeqFormatter.d(context, traits.asIntFamily());
      case UNSIGNED_DECIMAL_INTEGER:
        return SeqFormatter.u(context, traits.asIntFamily());
      case UNSIGNED_HEXADECIMAL_INTEGER:
        return SeqFormatter.x(context, traits.asIntFamily());
      case UNSIGNED_HEXADECIMAL_INTEGER_UPPERCASE:
        return SeqFormatter.x(context, traits.asIntFamily()).upperCase();
      case UNSIGNED_OCTAL_INTEGER:
        return SeqFormatter.o(context, traits.asIntFamily());
      case DECIMAL_FLOATING_POINT:
        return SeqFormatter.f(context, traits.asFloatFamily());
      case DECIMAL_FLOATING_POINT_UPPERCASE:
        return SeqFormatter.f(context, traits.asFloatFamily()).upperCase();
      case SCIENTIFIC_NOTATION:
        return SeqFormatter.e(context, traits.asFloatFamily());
      case SCIENTIFIC_NOTATION_UPPERCASE:
        return SeqFormatter.e(context, traits.asFloatFamily()).upperCase();
      case USE_SHORTEST_PRESENTATION:
        return SeqFormatter.g(context, traits.asFloatFamily());
      case USE_SHORTEST_PRESENTATION_UPPERCASE:
        return SeqFormatter.g(context, traits.asFloatFamily()).upperCase();
      case HEXADECIMAL_FLOATING_POINT:
        return SeqFormatter.a(context, traits.asFloatFamily());
      case HEXADECIMAL_FLOATING_POINT_UPPERCASE:
        return SeqFormatter.a(context, traits.asFloatFamily()).upperCase();
      case STRING:
        return SeqFormatter.s(context, traits);
      case STRING_UPPERCASE:
        return SeqFormatter.s(context, traits).upperCase();
      case CHARACTER:
        return SeqFormatter.c(context, traits);
      case POINTER:
        return SeqFormatter.p(context, traits);
      case PERCENT_SIGN:
        return Seq.singleChar('%');
      default:
        return Seq.empty();
    }
  }

  public Specifier getSpecifier() {
    return specifier;
  }

  public FormatContext getContext() {
    return context;
  }

  @Override
  public String toString() {
    return context.toPatternString(specifier);
  }
}
