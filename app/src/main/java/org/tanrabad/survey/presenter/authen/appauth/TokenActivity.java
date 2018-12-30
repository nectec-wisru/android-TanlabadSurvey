/*
 * Copyright (c) 2018 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tanrabad.survey.presenter.authen.appauth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceDiscovery;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.TokenRequest;

import org.tanrabad.survey.R;
import org.tanrabad.survey.presenter.LoginActivity;
import org.tanrabad.survey.presenter.TanrabadActivity;
import org.tanrabad.survey.presenter.authen.Authenticator;
import org.tanrabad.survey.presenter.authen.UserProfile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import okio.Okio;

public class TokenActivity extends TanrabadActivity {
    private static final String TAG = "TokenActivity";

    private static final String KEY_USER_INFO = "userInfo";
    public static final String USERNAME = "username";

    private AuthorizationService mAuthService;
    private AuthStateManager mStateManager;
    private UserProfileManager mUserManager;
    private final AtomicReference<UserProfile> mUserInfoJson = new AtomicReference<>();
    private ExecutorService mExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);

        mStateManager = AuthStateManager.getInstance(this);
        mUserManager = UserProfileManager.getInstance(this);
        mExecutor = Executors.newSingleThreadExecutor();

        Configuration config = Configuration.getInstance(this);
        mAuthService = new AuthorizationService(
            this,
            new AppAuthConfiguration.Builder()
                .setConnectionBuilder(config.getConnectionBuilder())
                .build());

        mUserInfoJson.compareAndSet(null, mUserManager.getProfile());

        findViewById(R.id.logout).setOnClickListener(v -> signOut());
        findViewById(R.id.authentication_button).setOnClickListener(v -> {
            UserProfile profile = mUserInfoJson.get();
            Authenticator auth = new Authenticator(null);
            auth.setAuthenWith(profile);

            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(USERNAME, profile.userName);
            setResult(RESULT_OK, intent);
            finish();
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mExecutor.isShutdown()) {
            mExecutor = Executors.newSingleThreadExecutor();
        }

        if (mStateManager.getCurrent().isAuthorized()) {
            if (mUserInfoJson.get() != null) {
                displayAuthorized();
            } else {
                mStateManager.getCurrent().performActionWithFreshTokens(mAuthService, this::fetchUserInfo);
            }
            return;
        }

        // the stored AuthState is incomplete, so check if we are currently receiving the result of
        // the authorization flow from the browser.
        AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException ex = AuthorizationException.fromIntent(getIntent());

        if (response != null || ex != null) {
            mStateManager.updateAfterAuthorization(response, ex);
        }

        if (response != null && response.authorizationCode != null) {
            // authorization code exchange is required
            mStateManager.updateAfterAuthorization(response, ex);
            exchangeAuthorizationCode(response);
        } else if (ex != null) {
            displayNotAuthorized("Authorization flow failed: " + ex.getMessage());
        } else {
            displayNotAuthorized("No authorization state retained - reauthorization required");
            signOut();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuthService.dispose();
        mExecutor.shutdownNow();
    }

    @MainThread
    private void displayNotAuthorized(String explanation) {
        Toast.makeText(this, explanation, Toast.LENGTH_SHORT).show();
    }

    @MainThread
    private void displayAuthorized() {
        AuthState state = mStateManager.getCurrent();
        Log.i(TAG, "Display Authorized");

        UserProfile userInfo = mUserInfoJson.get();
        if (userInfo != null) {
            Log.i(TAG, "user=" + userInfo.toString());
            ((TextView) findViewById(R.id.username)).setText(userInfo.userName);
            ((TextView) findViewById(R.id.user_fullname)).setText(userInfo.name);
            ((TextView) findViewById(R.id.organization)).setText(userInfo.orgName);
        }
    }


    @MainThread
    private void exchangeAuthorizationCode(AuthorizationResponse authorizationResponse) {
        Log.i(TAG, "Request to Exchange token");
        final TokenRequest request = authorizationResponse.createTokenExchangeRequest();
        performTokenRequest(
            request,
            (tokenResponse, authException) -> {
                mStateManager.updateAfterTokenResponse(tokenResponse, authException);
                if (!mStateManager.getCurrent().isAuthorized()) {
                    final String message = "Authorization Code exchange failed"
                        + ((authException != null) ? authException.error : "");
                    Log.e(TAG, message);
                    runOnUiThread(() -> displayNotAuthorized(message));
                } else {
                    runOnUiThread(this::displayAuthorized);
                    mStateManager.getCurrent().performActionWithFreshTokens(mAuthService, this::fetchUserInfo);
                }
            });
    }

    @MainThread
    private void performTokenRequest(
        TokenRequest request,
        AuthorizationService.TokenResponseCallback callback) {
        ClientAuthentication clientAuthentication;
        try {
            clientAuthentication = mStateManager.getCurrent().getClientAuthentication();
        } catch (ClientAuthentication.UnsupportedAuthenticationMethod ex) {
            Log.d(TAG, "Token request cannot be made, client authentication for the token "
                + "endpoint could not be constructed (%s)", ex);
            displayNotAuthorized("Client authentication method is unsupported");
            return;
        }

        mAuthService.performTokenRequest(
            request,
            clientAuthentication,
            callback);
    }

    @MainThread
    private void fetchUserInfo(final String accessToken, String idToken, AuthorizationException ex) {
        if (ex != null) {
            Log.e(TAG, "Token refresh failed when fetching user info");
            mUserInfoJson.set(null);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayAuthorized();
                }
            });
            return;
        }

        AuthorizationServiceDiscovery discovery =
            mStateManager.getCurrent()
                .getAuthorizationServiceConfiguration()
                .discoveryDoc;

        final URL userInfoEndpoint;
        try {
            userInfoEndpoint = new URL(discovery.getUserinfoEndpoint().toString());
        } catch (MalformedURLException urlEx) {
            Log.e(TAG, "Failed to construct user info endpoint URL", urlEx);
            mUserInfoJson.set(null);
            runOnUiThread(() -> displayAuthorized());
            return;
        }

        mExecutor.submit(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) userInfoEndpoint.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setInstanceFollowRedirects(false);
                String response = Okio.buffer(Okio.source(conn.getInputStream()))
                    .readString(Charset.forName("UTF-8"));
                mUserInfoJson.set(UserProfile.fromJson(response));
                mUserManager.save(mUserInfoJson.get());
            } catch (IOException ioEx) {
                Log.e(TAG, "Network error when querying userinfo endpoint", ioEx);
                TokenActivity.this.showSnackbar("Fetching user info failed");
            }
            runOnUiThread(this::displayAuthorized);
        });
    }

    @MainThread
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(R.id.coordinator),
            message,
            Snackbar.LENGTH_SHORT)
            .show();
    }

    @MainThread
    private void signOut() {
        // discard the authorization and token state, but retain the configuration and
        // dynamic client registration (if applicable), to save from retrieving them again.
        AuthState currentState = mStateManager.getCurrent();
        AuthState clearedState =
            new AuthState(currentState.getAuthorizationServiceConfiguration());
        if (currentState.getLastRegistrationResponse() != null) {
            clearedState.update(currentState.getLastRegistrationResponse());
        }
        mStateManager.replace(clearedState);
        mUserManager.clear();


        Intent mainIntent = new Intent(this, LoginActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }
}
