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

package org.tanrabad.survey.presenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import net.frakbot.jumpingbeans.JumpingBeans;
import org.tanrabad.survey.R;
import org.tanrabad.survey.TanrabadApp;
import org.tanrabad.survey.entity.Building;
import org.tanrabad.survey.entity.Place;
import org.tanrabad.survey.entity.lookup.*;
import org.tanrabad.survey.job.AbsJobRunner;
import org.tanrabad.survey.job.Job;
import org.tanrabad.survey.job.WritableRepoUpdateJob;
import org.tanrabad.survey.repository.*;
import org.tanrabad.survey.repository.persistence.*;
import org.tanrabad.survey.service.*;
import org.tanrabad.survey.utils.UserDataManager;
import org.tanrabad.survey.utils.alert.Alert;
import org.tanrabad.survey.utils.android.InternetConnection;
import org.tanrabad.survey.utils.prompt.AlertDialogPromptMessage;
import org.tanrabad.survey.utils.prompt.PromptMessage;

import java.io.IOException;

public class InitialActivity extends TanrabadActivity {

    private WritableRepoUpdateJob<Province> provinceUpdateJob = new WritableRepoUpdateJob<>(
            new ProvinceRestService(), DbProvinceRepository.getInstance());
    private WritableRepoUpdateJob<District> districtUpdateJob = new WritableRepoUpdateJob<>(
            new AmphurRestService(), DbDistrictRepository.getInstance());
    private WritableRepoUpdateJob<Subdistrict> subDistrictUpdateJob = new WritableRepoUpdateJob<>(
            new TambonRestService(), DbSubdistrictRepository.getInstance());
    private WritableRepoUpdateJob<PlaceType> placeTypeUpdateJob = new WritableRepoUpdateJob<>(
            new PlaceTypeRestService(), BrokerPlaceTypeRepository.getInstance());
    private WritableRepoUpdateJob<PlaceSubType> placeSubTypeUpdateJob = new WritableRepoUpdateJob<>(
            new PlaceSubTypeRestService(), BrokerPlaceSubTypeRepository.getInstance());
    private WritableRepoUpdateJob<ContainerType> containerTypeUpdateJob = new WritableRepoUpdateJob<>(
            new ContainerTypeRestService(), BrokerContainerTypeRepository.getInstance());
    private WritableRepoUpdateJob<ContainerLocation> containerLocationUpdateJob = new WritableRepoUpdateJob<>(
            new ContainerLocationRestService(), new DbContainerLocationRepository(TanrabadApp.getInstance()));
    private WritableRepoUpdateJob<Place> placeUpdateJob = new WritableRepoUpdateJob<>(
            new PlaceRestService(), BrokerPlaceRepository.getInstance());
    private WritableRepoUpdateJob<Building> buildingUpdateJob = new WritableRepoUpdateJob<>(
            new BuildingRestService(), BrokerBuildingRepository.getInstance());

    private TextView loadingText;
    private JumpingBeans pleaseWaitBeans;
    private ApiSyncInfoPreference syncInfoPreference;
    private InitialActivityController initialActivityController;

    public static void open(Activity activity) {
        Intent intent = new Intent(activity, InitialActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        loadingText = (TextView) findViewById(R.id.loading);
        startPleaseWaitBeansJump();
        syncInfoPreference = new ApiSyncInfoPreference(InitialActivity.this);
        InternetConnection internetConnection = new InternetConnection(this);

        initialActivityController = new InitialActivityController(
                internetConnection, syncInfoPreference) {
            @Override
            public void onFail() {
                UserDataManager.clearAll(InitialActivity.this);
                PromptMessage promptMessage = new AlertDialogPromptMessage(InitialActivity.this);
                promptMessage.setOnConfirm(getString(R.string.back_to_login), new PromptMessage.OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        Intent intent = new Intent(InitialActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
                promptMessage.show(null, getString(R.string.download_first_time_fail));
            }

            @Override
            public void onSuccess() {
                MainActivity.open(InitialActivity.this);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }

            @Override
            protected void downloadData() {
                new InitialJobRunner()
                        .addJob(new CreateDatabaseJob(InitialActivity.this))
                        .addJob(containerLocationUpdateJob)
                        .addJob(containerTypeUpdateJob)
                        .addJob(provinceUpdateJob)
                        .addJob(districtUpdateJob)
                        .addJob(subDistrictUpdateJob)
                        .addJob(placeTypeUpdateJob)
                        .addJob(placeSubTypeUpdateJob)
                        .addJob(placeUpdateJob)
                        .addJob(buildingUpdateJob)
                        .start();
            }
        };

        initialActivityController.startInitialData();
    }

    private void startPleaseWaitBeansJump() {
        pleaseWaitBeans = JumpingBeans.with((TextView) findViewById(R.id.please_wait))
                .appendJumpingDots()
                .build();
    }

    @SuppressLint("SetTextI18n")
    private void updateLoadingText(Job startingJob) {
        switch (startingJob.getId()) {
            case CreateDatabaseJob.ID:
                loadingText.setText("เตรียมกระดาษสำหรับจดข้อมูล");
                break;
            case WritableRepoUpdateJob.ID:
                updateLoadingTextByWritableJobInstance(startingJob);
                break;
            default:
                loadingText.setText("มายังไงหละนี้");
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateLoadingTextByWritableJobInstance(Job startingJob) {
        if (startingJob.equals(provinceUpdateJob))
            loadingText.setText("รอพบกับท่านผู้ว่า");
        else if (startingJob.equals(districtUpdateJob))
            loadingText.setText("ไปหานายอำเภอ");
        else if (startingJob.equals(subDistrictUpdateJob))
            loadingText.setText("กินข้าวกับ อบต.");
        else if (startingJob.equals(placeTypeUpdateJob))
            loadingText.setText("เข้าเทศบาล");
        else if (startingJob.equals(placeSubTypeUpdateJob))
            loadingText.setText("คุยกับคนพื้นที่");
        else if (startingJob.equals(containerTypeUpdateJob))
            loadingText.setText("ค้นกระเป๋าโดเรม่อน");
        else if (startingJob.equals(containerLocationUpdateJob))
            loadingText.setText("หาตำแหน่งเป้าหมาย");
        else if (startingJob.equals(placeUpdateJob))
            loadingText.setText("สแกนพื้นที่ปฎิบัติงาน");
        else if (startingJob.equals(buildingUpdateJob))
            loadingText.setText("เตรียมตัวกำจัดเหล่าร้าย");
    }

    public class InitialJobRunner extends AbsJobRunner {

        private IOException ioException;
        private RestServiceException restServiceException;

        @Override
        protected void onJobError(Job errorJob, Exception exception) {
            super.onJobError(errorJob, exception);
            if (exception instanceof IOException)
                ioException = (IOException) exception;
            else if (exception instanceof RestServiceException)
                restServiceException = (RestServiceException) exception;
            if (InternetConnection.isAvailable(InitialActivity.this)) TanrabadApp.log(exception);
        }

        @Override
        protected void onJobStart(Job startingJob) {
            updateLoadingText(startingJob);
        }

        @Override
        protected void onRunFinish() {
            pleaseWaitBeans.stopJumping();

            initialActivityController.downloadComplete(isSyncSuccess());
            showErrorMessage();
        }

        private boolean isSyncSuccess() {
            return ioException == null && restServiceException == null;
        }

        private void showErrorMessage() {
            if (ioException != null)
                Alert.mediumLevel().show(R.string.error_connection_problem);
            else if (restServiceException != null)
                Alert.mediumLevel().show(R.string.error_rest_service);
        }
    }
}
