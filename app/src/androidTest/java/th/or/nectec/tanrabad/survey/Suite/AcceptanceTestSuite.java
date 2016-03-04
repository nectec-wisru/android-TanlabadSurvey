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

package th.or.nectec.tanrabad.survey.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import th.or.nectec.tanrabad.survey.presenter.*;
import th.or.nectec.tanrabad.survey.presenter.view.EditTextStepperTest;
import th.or.nectec.tanrabad.survey.presenter.view.TorchButtonTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MainStartSurveyTest.class,

        PlaceListInDatabaseTest.class,
        PlaceMapMarkerActivityTest.class,
        PlaceFormActivityByPutExtraPlaceTypeSchoolTest.class,
        UpdatePlaceTest.class,
        PlaceSearchActivityTest.class,

        BuildingListActivityAtBuildingEmptyTest.class,
        BuildingListActivityAtBuildingNotEmptyTest.class,
        BuildingFormActivityTest.class,
        BuildingMapMarkerActivityTest.class,
        UpdateBuildingTest.class,

        SurveyActivityTest.class,

        EditTextStepperTest.class,
        TorchButtonTest.class,
        })
public class AcceptanceTestSuite {
}
