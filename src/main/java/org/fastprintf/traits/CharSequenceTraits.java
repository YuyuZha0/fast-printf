package org.fastprintf.traits;

public final class CharSequenceTraits extends AbstractTextTraits {

  private final CharSequence value;

  public CharSequenceTraits(CharSequence value) {
    this.value = value;
  }

  @Override
  public CharSequence asCharSequence() {
    return value;
  }
}
