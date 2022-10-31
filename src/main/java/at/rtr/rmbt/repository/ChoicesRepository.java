package at.rtr.rmbt.repository;

import at.rtr.rmbt.utils.QueryParser;

import java.util.Set;

public interface ChoicesRepository {
    Set<String> findCountryMobile(QueryParser queryParser);

    Set<String> findProviderMobile(QueryParser queryParser);

    Set<String> findProvider(QueryParser queryParser);
}
