package io.burt.jmespath.function;

import java.util.Iterator;

import io.burt.jmespath.Adapter;

/**
 * A description of the expected type of an argument or list of arguments passed
 * to a function.
 */
public interface ArgumentConstraint {
  /**
   * Check that the argument list complies with the constraints.
   * <p>
   * Most constraints will consume one or more elements from the iterator, but
   * constraints that represents optional arguments may choose not to.
   *
   * @throws ArityException when there are not enough arguments left to satisfy the constraint
   * @throws ArgumentTypeException when an argument does not satisfy the constraint
   */
  <T> void check(Adapter<T> runtime, Iterator<FunctionArgument<T>> arguments);

  /**
   * @return the minimum number of arguments required.
   */
  int minArity();

  /**
   * @return the maximum number of arguments accepted.
   */
  int maxArity();

  /**
   * @return a string representation of the types accepted. Used to construct
   *   user friendly error messages.
   */
  String expectedType();
}
