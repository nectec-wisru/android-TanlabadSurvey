/*
 * Copyright (c) 2016 NECTEC
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

package th.or.nectec.tanrabad.survey.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.widget.CheckBox;
import th.or.nectec.tanrabad.survey.BuildConfig;
import th.or.nectec.tanrabad.survey.R;
import th.or.nectec.tanrabad.survey.TanrabadApp;
import th.or.nectec.tanrabad.survey.presenter.authen.AuthenActivity;
import th.or.nectec.tanrabad.survey.repository.StubUserRepository;
import th.or.nectec.tanrabad.survey.service.PlaceRestService;
import th.or.nectec.tanrabad.survey.service.ServiceLastUpdatePreference;
import th.or.nectec.tanrabad.survey.utils.alert.Alert;
import th.or.nectec.tanrabad.survey.utils.android.InternetConnection;
import th.or.nectec.tanrabad.survey.utils.showcase.ShowcasePreference;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.animation.AnimationUtils.loadAnimation;

public class LoginActivity extends TanrabadActivity {
    private static final int AUTHEN_REQUEST_CODE = 1232;
    private CheckBox needShowcase;
    private ShowcasePreference showcasePreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupShowcaseOption();

        findViewById(R.id.authentication_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // anonymousLogin();
                openAuthenWeb();
            }
        });
        startAnimation();
    }

    private void setupShowcaseOption() {
        showcasePreference = new ShowcasePreference(this);
        needShowcase = (CheckBox) findViewById(R.id.need_showcase);
        needShowcase.setChecked(showcasePreference.get());
    }

    private void openAuthenWeb() {
        Intent intent = new Intent(this, AuthenActivity.class);
        startActivityForResult(intent, AUTHEN_REQUEST_CODE);
    }

    private void startAnimation() {
        findViewById(R.id.bg_blue).startAnimation(loadAnimation(this, R.anim.login_bg_blue));
        Animation dropIn = loadAnimation(this, R.anim.logo);
        dropIn.setStartOffset(1200);
        findViewById(R.id.logo_tabrabad).startAnimation(dropIn);
    }

    private void anonymousLogin() {
        if (isFirstTime() && !InternetConnection.isAvailable(LoginActivity.this)) {
            Alert.highLevel().show(R.string.connect_internet_when_use_for_first_time);
            TanrabadApp.action().firstTimeWithoutInternet();
        } else {
            AccountUtils.setUser(new StubUserRepository().findByUsername(BuildConfig.USER));
            showcasePreference.save(needShowcase.isChecked());
            InitialActivity.open(LoginActivity.this);
            finish();
        }
    }

    private boolean isFirstTime() {
        String placeTimeStamp = new ServiceLastUpdatePreference(LoginActivity.this, PlaceRestService.PATH).get();
        return TextUtils.isEmpty(placeTimeStamp);
    }
}
