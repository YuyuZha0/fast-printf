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
import java.util.function.BiFunction;

public final class DefaultAppender implements Appender {

  private final Specifier specifier;
  private final FormatContext context;
  private final BiFunction<FormatContext, FormatTraits, Seq> formatter;

  public DefaultAppender(Specifier specifier, FormatContext context) {
    this.specifier = Objects.requireNonNull(specifier, "specifier");
    this.context = Objects.requireNonNull(context, "context");
    this.formatter = formatterForSpecifier(specifier);
  }

  private static BiFunction<FormatContext, FormatTraits, Seq> formatterForSpecifier(
      Specifier specifier) {
    switch (specifier) {
      case SIGNED_DECIMAL_INTEGER:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.d(context, traits.asIntFamily());
      case UNSIGNED_DECIMAL_INTEGER:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.u(context, traits.asIntFamily());
      case UNSIGNED_HEXADECIMAL_INTEGER:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.x(context, traits.asIntFamily());
      case UNSIGNED_HEXADECIMAL_INTEGER_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.x(context, traits.asIntFamily()).upperCase();
      case UNSIGNED_OCTAL_INTEGER:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.o(context, traits.asIntFamily());
      case DECIMAL_FLOATING_POINT:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.f(context, traits.asFloatFamily());
      case DECIMAL_FLOATING_POINT_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.f(context, traits.asFloatFamily()).upperCase();
      case SCIENTIFIC_NOTATION:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.e(context, traits.asFloatFamily());
      case SCIENTIFIC_NOTATION_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.e(context, traits.asFloatFamily()).upperCase();
      case USE_SHORTEST_PRESENTATION:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.g(context, traits.asFloatFamily());
      case USE_SHORTEST_PRESENTATION_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.g(context, traits.asFloatFamily()).upperCase();
      case HEXADECIMAL_FLOATING_POINT:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.a(context, traits.asFloatFamily());
      case HEXADECIMAL_FLOATING_POINT_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.a(context, traits.asFloatFamily()).upperCase();
      case STRING:
        return SeqFormatter::s;
      case STRING_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.s(context, traits).upperCase();
      case CHARACTER:
        return SeqFormatter::c;
      case POINTER:
        return SeqFormatter::p;
      case PERCENT_SIGN:
        return (FormatContext context, FormatTraits traits) -> Seq.singleChar('%');
      default:
        return (FormatContext context, FormatTraits traits) -> Seq.empty();
    }
  }

  private int nextInt(Iterator<FormatTraits> iterator) {
    if (!iterator.hasNext()) {
      throw new NoSuchElementException("Missing argument for specifier: " + specifier);
    }
    return iterator.next().asInt();
  }

  @Override
  public void append(List<Seq> collect, Iterator<FormatTraits> traitsIterator) {
    FormatContext context = this.context;
    if (context.isPrecedingWidth()) {
      int w = nextInt(traitsIterator);
      if (w >= 0) {
        context = context.setWidth(w);
      } else {
        context = context.addFlag(Flag.LEFT_JUSTIFY).setWidth(-w);
      }
    }
    if (context.isPrecedingPrecision()) {
      int p = nextInt(traitsIterator);
      context = context.setPrecision(p >= 0 ? p : FormatContext.UNSET);
    }
    if (!traitsIterator.hasNext()) {
      throw new NoSuchElementException("Missing argument for specifier: " + specifier);
    }
    collect.add(format(context, traitsIterator.next()));
  }

  private Seq format(FormatContext context, FormatTraits traits) {
    if (traits.isNull()) {
      return SeqFormatter.forNull(context);
    }
    return formatter.apply(context, traits);
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
