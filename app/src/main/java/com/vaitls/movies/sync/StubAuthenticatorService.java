package com.vaitls.movies.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * The service which allows the sync adapter framework to access the
 * authenticator.
 */
public class StubAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private StubAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new StubAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    static class StubAuthenticator extends AbstractAccountAuthenticator {

        public StubAuthenticator(Context context) {
            super(context);
        }

        // No properties to edit.
        @Override
        public Bundle editProperties(
            AccountAuthenticatorResponse r, String s) {
            throw new UnsupportedOperationException();
        }

        // Because we're not actually adding an account to the device, just return null.
        @Override
        public Bundle addAccount(
            AccountAuthenticatorResponse r,
            String s,
            String s2,
            String[] strings,
            Bundle bundle) throws NetworkErrorException {
            return null;
        }

        // Ignore attempts to confirm credentials
        @Override
        public Bundle confirmCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            Bundle bundle) throws NetworkErrorException {
            return null;
        }

        // Getting an authentication token is not supported
        @Override
        public Bundle getAuthToken(
            AccountAuthenticatorResponse r,
            Account account,
            String s,
            Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        // Getting a label for the auth token is not supported
        @Override
        public String getAuthTokenLabel(String s) {
            throw new UnsupportedOperationException();
        }

        // Updating user credentials is not supported
        @Override
        public Bundle updateCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            String s, Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        // Checking features for the account is not supported
        @Override
        public Bundle hasFeatures(
            AccountAuthenticatorResponse r,
            Account account, String[] strings) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }
    }
}
