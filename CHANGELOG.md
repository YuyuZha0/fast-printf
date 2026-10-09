# Changelog

## 1.2.14 (unreleased)

### Performance and structure

- Add optional, automatically detected Jackson 2 `JsonNode` argument support and README examples
  for tree values and annotation-based property serialization. Core formatting still runs without Jackson.
- Consolidate sign, base prefix, precision, zero padding, and alignment in one numeric layout rule.
- Resolve dynamic width and precision in one context, sharing immutable flags where possible.
- Render numeric uppercase markers directly and uppercase only digit segments that need it.
- Count significant digits without repeatedly traversing every digit in a composite sequence.
- Add deterministic, focused JMH workloads and a manual benchmark workflow.

### Correctness

- Preserve zero in `%#.0o`, count the octal leading zero toward precision, and avoid reserving
  a nonexistent hexadecimal prefix when formatting zero.
- Preserve the space sign flag when zero integer digits are suppressed by precision zero.
- Retain the decimal point and significant trailing zeros in alternate scientific `%g/%G`.
- Choose BigDecimal `%g` notation after rounding and normalize scaled zero in `%g` and `%e`.
- Preserve the declared length of slices spanning multiple sequence segments.
- Reject `Integer.MIN_VALUE` dynamic width instead of overflowing when taking its magnitude.
- Honor the space sign flag for positive infinity.
- Protect cached builders from recursive formatting and release them after exceptions.
- Document the finite-value precondition for internal numeric layouts and verify special-value
  handling at the formatting entry point. Strengthen floating-point tests with exact comparisons.

The existing `NaN`/`Infinity` spelling, ASCII uppercase behavior, and Java-compatible `%.0a`
precision convention remain unchanged. Corrected edge-case output can differ from 1.2.13.

### Build and release

- Bump the patch version from 1.2.13 to 1.2.14; runtime baseline remains Java 8.
- Enforce canonical JDK 21 coverage floors and set Codecov project/patch targets to 95%.
- Preserve JDK 8/11/17/21 CI, upload test reports, cancel superseded CI runs, and submit the
  dependency graph only on main-branch pushes.
- Validate release tags against the Maven version in CI, correct ahead/behind diagnostics in
  `create_tag.sh`, and use JDK 21 and environment-based signing credentials for publication.
- Publish only on a published GitHub Release or explicit workflow dispatch; wait for Central's
  publication result instead of stopping after upload/validation.
