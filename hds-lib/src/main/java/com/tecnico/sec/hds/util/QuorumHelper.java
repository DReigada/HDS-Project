package com.tecnico.sec.hds.util;

import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.TransactionInformation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuorumHelper {
  int servers;

  public QuorumHelper(int servers) {
    this.servers = servers;
  }

  /**
   * @param serversWithResponsesStream a stream of servers and responses
   * @param responseToList             a function to transform a server response to a list of transactions
   * @param <RES>                      he server response type
   * @return list tuple that contains the quorum response and the servers that are missing transactions (to be used in the write back)
   */
  public <RES> Optional<Tuple<Tuple<DefaultApi, RES>, List<Tuple<DefaultApi, RES>>>> getReadQuorumFromResponses(
      Stream<Tuple<DefaultApi, RES>> serversWithResponsesStream,
      Function<RES, List<TransactionInformation>> responseToList,
      Predicate<Tuple<DefaultApi, RES>> responseVerifier
  ) {
    Stream<Tuple<DefaultApi, RES>> validatedResponses =
        serversWithResponsesStream
            .filter(responseVerifier);

    return getTransactionsQuorum(validatedResponses, a -> responseToList.apply(a.second), getServersThreshold());
  }

  /**
   * @param responses      the responses from which to get the majority
   * @param responseToList a function to transform a response to a List<TransactionInformation>>
   * @param threshold      the majority size
   * @return And empty optional if a majority does not exist, or else a Tuple with the majority
   * and the responses that are not in the majority (this can be used to know which servers need the write back)
   */
  private <A> Optional<Tuple<A, List<A>>> getTransactionsQuorum(Stream<A> responses, Function<A, List<TransactionInformation>> responseToList, int threshold) {
    Collection<List<Tuple<A, List<TransactionInformation>>>> groupedResponses =
        responses
            .map(a -> new Tuple<>(a, responseToList.apply(a)))
            .collect(Collectors.groupingBy(aWithTransactions ->
                aWithTransactions.second.size() > 0 ? aWithTransactions.second.get(0).getSendHash().getValue() : ""))
            .values();

    Optional<A> quorumOpt = groupedResponses.stream()
        .filter(list -> list.size() > threshold)
        .map(list -> list.get(0).first)
        .findFirst();

    List<A> notInQuorum = groupedResponses.stream()
        .filter(list -> !(list.size() > threshold))
        .flatMap(list -> list.stream().map(a -> a.first))
        .collect(Collectors.toList());

    return quorumOpt.map(quorum -> new Tuple<>(quorum, notInQuorum));
  }

  private int getServersThreshold() {
    return (int) ((servers + getNumberOfFaults(servers)) / 2.0);
  }

  private int getNumberOfFaults(int serversNumber) {
    return 1; // TODO change this
  }

}
