package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class AvgFunction extends ArrayMathFunction {
  @Override
  protected <T> boolean isValidArray(Adapter<T> adapter, T array) {
    return TypesHelper.isNumberArray(adapter, array);
  }

  @Override
  protected String expectedType() {
    return "array of numbers";
  }

  @Override
  protected <T> T performMathOperation(Adapter<T> adapter, List<T> values) {
    if (values.isEmpty()) {
      return adapter.createNull();
    } else {
      double sum = 0;
      int count = 0;
      for (T n : values) {
        sum += adapter.toNumber(n).doubleValue();
        count += 1;
      }
      return adapter.createNumber(sum/count);
    }
  }
}
