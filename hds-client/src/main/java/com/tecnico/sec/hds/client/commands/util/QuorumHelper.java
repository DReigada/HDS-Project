package com.tecnico.sec.hds.client.commands.util;

import io.swagger.client.model.TransactionInformation;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.tecnico.sec.hds.util.Tuple;

public class QuorumHelper {

  /**
   * @param responses
   * @param responseToList a function to transform a response to a List<TransactionInformation>>
   * @param threshold      the majority size
   * @return the fist element of the stream is the response that matches the majority of the responses,
   * the rest of the stream are the responses that do not have a sufficient number of transactions
   * (this can be used to know which servers need the write back)
   */
  public static <A> Stream<A> getTransactionsQuorum(List<A> responses, Function<A, List<TransactionInformation>> responseToList, int threshold) {
    Comparator<Tuple<A, Integer>> reversedComparator =
        Comparator.comparingInt((Tuple<A, Integer> tuple) -> tuple.second).reversed();

    return responses.stream()
        .map(a -> new Tuple<>(a, responseToList.apply(a).size()))
        .sorted(reversedComparator)
        .skip(threshold)
        .map(a -> a.first);
  }
}
