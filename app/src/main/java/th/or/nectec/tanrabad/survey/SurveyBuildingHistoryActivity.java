package th.or.nectec.tanrabad.survey;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import th.or.nectec.tanrabad.domain.SurveyBuildingHistoryController;
import th.or.nectec.tanrabad.domain.SurveyBuildingPresenter;
import th.or.nectec.tanrabad.entity.Building;
import th.or.nectec.tanrabad.survey.repository.InMemorySurveyRepository;
import th.or.nectec.tanrabad.survey.repository.StubPlaceRepository;
import th.or.nectec.tanrabad.survey.repository.StubUserRepository;

public class SurveyBuildingHistoryActivity extends TanrabadActivity {

    public static final String SURVEY_ARG = "survey_arg";

    private TextView placeName;
    private ListView surveyBuildingHistoryList;
    private SurveyBuildingHistoryAdapter surveyBuildingHistoryAdapter;
    private SurveyBuildingHistoryController surveyBuildingHistoryController;
    private SurveyBuildingPresenter surveyBuildingPresenter = new SurveyBuildingPresenter() {
        @Override
        public void alertUserNotFound() {
            Toast.makeText(SurveyBuildingHistoryActivity.this, R.string.user_not_found, Toast.LENGTH_LONG).show();
        }

        @Override
        public void alertPlaceNotFound() {
            Toast.makeText(SurveyBuildingHistoryActivity.this, R.string.place_not_found, Toast.LENGTH_LONG).show();
        }

        @Override
        public void alertSurveyBuildingsNotFound() {
            Toast.makeText(SurveyBuildingHistoryActivity.this, R.string.survey_building_history_not_found, Toast.LENGTH_LONG).show();
        }

        @Override
        public void displaySurveyBuildingList(List<Building> buildings) {
            surveyBuildingHistoryAdapter = new SurveyBuildingHistoryAdapter(SurveyBuildingHistoryActivity.this, buildings);
            surveyBuildingHistoryList.setAdapter(surveyBuildingHistoryAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_building_list);

        placeName = (TextView) findViewById(R.id.place_name);
        surveyBuildingHistoryList = (ListView) findViewById(R.id.survey_building_history_list);

        surveyBuildingHistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Building building = surveyBuildingHistoryAdapter.getItem(position);
                bringToSurveyActivity(building);
            }
        });

        showSurveyBuildingHistoryList();
    }

    private void showSurveyBuildingHistoryList() {
        surveyBuildingHistoryController = new SurveyBuildingHistoryController(new StubUserRepository(),
                new StubPlaceRepository(),
                InMemorySurveyRepository.getInstance(),
                this.surveyBuildingPresenter);
        surveyBuildingHistoryController.showSurveyBuildingOf(getUuidFromIntent(), getUserNameFromIntent());
    }

    private void bringToSurveyActivity(Building building) {
        Intent intent = new Intent(SurveyBuildingHistoryActivity.this, SurveyActivity.class);
        intent.putExtra(SurveyActivity.BUILDING_UUID_ARG, building.getId().toString());
        intent.putExtra(SurveyActivity.USERNAME_ARG, "sara");
        startActivity(intent);
    }

    private String getUuidFromIntent() {
        return getIntent().getStringExtra("placeUUID");
    }

    private String getUserNameFromIntent() {
        return getIntent().getStringExtra("username");
    }

}


