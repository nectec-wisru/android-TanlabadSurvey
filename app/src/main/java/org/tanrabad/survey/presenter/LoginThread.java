package org.tanrabad.survey.presenter;

import android.os.Handler;
import android.os.Message;

import org.tanrabad.survey.BuildConfig;
import org.tanrabad.survey.TanrabadApp;
import org.tanrabad.survey.entity.User;
import org.tanrabad.survey.job.SetTrialModeAndSelectApiServerJob;
import org.tanrabad.survey.job.UploadJobBuilder;
import org.tanrabad.survey.service.AbsRestService;
import org.tanrabad.survey.utils.UserDataManager;
import org.tanrabad.survey.utils.android.InternetConnection;

class LoginThread implements Runnable {
    private static final int SUCCESS = 1;
    private static final int FAIL = 0;
    private final User user;
    private final Handler handler;


    public LoginThread(final User user, LoginListener loginListener) {
        this.user = user;
        handler = new LoginHandler(loginListener);
    }

    public void run() {
        if (InternetConnection.isAvailable(TanrabadApp.getInstance())) {
            if (shouldUploadOldUserData(user)) {
                setApiEndPointByUser(AccountUtils.getLastLoginUser());
                syncAndClearData();
            }
        } else if (!user.equals(AccountUtils.getLastLoginUser())) {
            handler.sendEmptyMessage(FAIL);
            return;
        }

        AccountUtils.setUser(user);
        setApiEndPointByUser(user);
        handler.sendEmptyMessage(SUCCESS);
    }

    private void syncAndClearData() {
        try {
            UploadJobBuilder uploadJobBuilder = new UploadJobBuilder();
            uploadJobBuilder.placePostDataJob.execute();
            uploadJobBuilder.buildingPostDataJob.execute();
            uploadJobBuilder.surveyPostDataJob.execute();
            uploadJobBuilder.placePutDataJob.execute();
            uploadJobBuilder.buildingPutDataJob.execute();
            uploadJobBuilder.surveyPutDataJob.execute();
            UserDataManager.clearAll(TanrabadApp.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static boolean shouldUploadOldUserData(User user) {
        if (AccountUtils.getLastLoginUser() == null)
            return false;

        return user.getOrganizationId() != AccountUtils.getLastLoginUser().getOrganizationId();
    }

    private static void setApiEndPointByUser(User user) {
        if (!AccountUtils.isTrialUser(user)) {
            AbsRestService.setBaseApi(BuildConfig.API_URL);
        } else {
            AbsRestService.setBaseApi(SetTrialModeAndSelectApiServerJob.TEST_URL);
        }
    }


    public interface LoginListener {
        void loginFinish();

        void loginFail();
    }

    static class LoginHandler extends Handler {
        private static final int SUCCESS = 1;
        private static final int FAIL = 0;
        private final LoginListener loginListener;

        public LoginHandler(LoginListener loginListener) {
            this.loginListener = loginListener;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    loginListener.loginFinish();
                    break;
                case FAIL:
                    loginListener.loginFail();
                    break;
            }
        }
    }
}
