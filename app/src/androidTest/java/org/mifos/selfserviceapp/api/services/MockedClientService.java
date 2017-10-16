package org.mifos.selfserviceapp.api.services;

import org.mifos.selfserviceapp.FakeRemoteDataSource;
import org.mifos.selfserviceapp.models.Page;
import org.mifos.selfserviceapp.models.client.Client;
import org.mifos.selfserviceapp.models.client.ClientAccounts;

import okhttp3.ResponseBody;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by dilpreet on 21/7/17.
 */

public class MockedClientService implements ClientService {
    @Override
    public Observable<Page<Client>> getClients() {
        return null;
    }

    @Override
    public Observable<Client> getClientForId(@Path(CLIENT_ID) long clientId) {
        return Observable.just(FakeRemoteDataSource.getCurrentClientAccount());
    }

    @Override
    public Observable<ResponseBody> getClientImage(@Path(CLIENT_ID) long clientId) {
        return Observable.error(new RuntimeException());
    }

    @Override
    public Observable<ClientAccounts> getClientAccounts(@Path(CLIENT_ID) long clientId) {
        return Observable.just(FakeRemoteDataSource.getClientAccounts());
    }

    @Override
    public Observable<ClientAccounts> getAccounts(@Path(CLIENT_ID) long clientId,
                                                  @Query("fields") String accountType) {
        return null;
    }
}
