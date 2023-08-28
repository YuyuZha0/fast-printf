package io.fastprintf.appender;

import io.fastprintf.Flag;
import io.fastprintf.FormatContext;
import io.fastprintf.PrintfException;
import io.fastprintf.Specifier;
import io.fastprintf.seq.Seq;
import io.fastprintf.traits.FormatTraits;
import io.fastprintf.util.Preconditions;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public final class DefaultAppender implements Appender {

  private final Specifier specifier;
  private final FormatContext context;
  private final BiFunction<FormatContext, FormatTraits, Seq> formatter;

  public DefaultAppender(Specifier specifier, FormatContext context) {
    this.specifier = Preconditions.checkNotNull(specifier, "specifier");
    this.context = Preconditions.checkNotNull(context, "context");
    this.formatter = formatterForSpecifier(specifier);
  }

  private static BiFunction<FormatContext, FormatTraits, Seq> formatterForSpecifier(
      Specifier specifier) {
    switch (specifier) {
      case SIGNED_DECIMAL_INTEGER:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.d(context, traits.asIntForm());
      case UNSIGNED_DECIMAL_INTEGER:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.u(context, traits.asIntForm());
      case UNSIGNED_HEXADECIMAL_INTEGER:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.x(context, traits.asIntForm());
      case UNSIGNED_HEXADECIMAL_INTEGER_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.x(context, traits.asIntForm()).upperCase();
      case UNSIGNED_OCTAL_INTEGER:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.o(context, traits.asIntForm());
      case DECIMAL_FLOATING_POINT:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.f(context, traits.asFloatForm());
      case DECIMAL_FLOATING_POINT_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.f(context, traits.asFloatForm()).upperCase();
      case SCIENTIFIC_NOTATION:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.e(context, traits.asFloatForm());
      case SCIENTIFIC_NOTATION_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.e(context, traits.asFloatForm()).upperCase();
      case USE_SHORTEST_PRESENTATION:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.g(context, traits.asFloatForm());
      case USE_SHORTEST_PRESENTATION_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.g(context, traits.asFloatForm()).upperCase();
      case HEXADECIMAL_FLOATING_POINT:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.a(context, traits.asFloatForm());
      case HEXADECIMAL_FLOATING_POINT_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.a(context, traits.asFloatForm()).upperCase();
      case STRING:
        return SeqFormatter::s;
      case STRING_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.s(context, traits).upperCase();
      case DATE_AND_TIME:
        return SeqFormatter::t;
      case DATE_AND_TIME_UPPERCASE:
        return (FormatContext context, FormatTraits traits) ->
            SeqFormatter.t(context, traits).upperCase();
      case CHARACTER:
        return SeqFormatter::c;
      case POINTER:
        return SeqFormatter::p;
      case PERCENT_SIGN:
        return (FormatContext context, FormatTraits traits) -> Seq.ch('%');
      default:
        return (FormatContext context, FormatTraits traits) -> Seq.empty();
    }
  }

  private int nextInt(Iterator<FormatTraits> iterator) {
    if (!iterator.hasNext()) {
      throw new PrintfException("Missing argument for specifier: %s", specifier);
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
    if (traitsIterator.hasNext()) {
      collect.add(format(context, traitsIterator.next()));
    } else {
      throw new PrintfException("Missing argument for specifier: " + specifier);
    }
  }

  private Seq format(FormatContext context, FormatTraits traits) {
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
