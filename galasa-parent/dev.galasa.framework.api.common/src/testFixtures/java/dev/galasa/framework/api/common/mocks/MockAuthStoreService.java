/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;
import dev.galasa.framework.spi.auth.UserDoc;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.FrontendClient;

public class MockAuthStoreService implements IAuthStoreService {

    List<IInternalAuthToken> tokens = new ArrayList<>();
    private ITimeService timeService;
    private int tokenIdCounter = 0;

    private boolean throwException = false;

    public MockAuthStoreService(List<IInternalAuthToken> tokens) {
        this.tokens = tokens;
        this.timeService = new MockTimeService(Instant.now());
    }

    public MockAuthStoreService(ITimeService timeService) {
        this.timeService = timeService;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    @Override
    public List<IInternalAuthToken> getTokens() throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }
        return tokens;
    }

    @Override
    public void storeToken(String clientId, String description, IInternalUser owner) throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }
        tokens.add(new MockInternalAuthToken("token-" + tokenIdCounter, description, timeService.now(), owner, clientId));
        tokenIdCounter++;
    }

    private void throwAuthStoreException() throws AuthStoreException {
        throw new AuthStoreException("simulating an unexpected failure!");
    }

    @Override
    public void deleteToken(String tokenId) throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }

        IInternalAuthToken tokenToRemove = getToken(tokenId);
        if (tokenToRemove != null) {
            tokens.remove(tokenToRemove);
        } else {
            throw new AuthStoreException("did not find token to delete!");
        }
    }

    @Override
    public IInternalAuthToken getToken(String tokenId) throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }

        IInternalAuthToken tokenToReturn = null;
        for (IInternalAuthToken token : tokens) {
            if (token.getTokenId().equals(tokenId)) {
                tokenToReturn = token;
                break;
            }
        }
        return tokenToReturn;
    }

    @Override
    public List<IInternalAuthToken> getTokensByLoginId(String loginId) throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }
        List<IInternalAuthToken> tokensToReturn = new ArrayList<>();
        for (IInternalAuthToken token : tokens) {
            if (token.getOwner().getLoginId().equals(loginId)) {
                tokensToReturn.add(token);
            }
        }
        return tokensToReturn;
    }

    @Override
    public List<UserDoc> getAllUsers() throws AuthStoreException {
        
        List<UserDoc> users = new ArrayList<>();

        UserDoc user1 = new UserDoc("user-1");
        UserDoc user2 = new UserDoc("user-2");

        user1.setUserNumber("docid");
        user1.setVersion("revVersion");
        user1.setClients(List.of(new FrontendClient("web-ui", Instant.parse("2024-10-18T14:49:50.096329Z"))));

        user2.setUserNumber("docid-2");
        user2.setVersion("revVersion2");
        user2.setClients(List.of(new FrontendClient("rest-api", Instant.parse("2024-10-18T14:49:50.096329Z"))));

        users.add(user1);
        users.add(user2);

        return users;
    }

    @Override
    public void createUser(String loginId, String clientName) throws AuthStoreException {
        // Do nothing
    }

    @Override
    public UserDoc getUserByLoginId(String loginId) throws AuthStoreException {
        
        UserDoc user = new UserDoc(loginId);

        user.setUserNumber("docid");
        user.setVersion("revVersion");
        user.setClients(List.of(new FrontendClient("web-ui", Instant.parse("2024-10-18T14:49:50.096329Z"))));

        return user;
    }

    @Override
    public void updateUserClientActivity(String loginId, String clientName) throws AuthStoreException {
        // Do nothing
    }
}
