/*
 * Copyright (c) 2015 NECTEC
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

package th.or.nectec.tanrabad.domain;

/**
 * Created by blaze on 11/9/2015 AD.
 */
public class SurveyResult {
    public static final int INDOOR = 1;
    public static final int OUTDOOR = 2;

    private final Container container;
    private final int location;
    private final int total;
    private final int found;

    public SurveyResult(Container container, int location, int total, int found) {

        this.container = container;
        this.location = location;
        this.total = total;
        this.found = found;
    }

    public static SurveyResult fromResult(Container container, int location, int total, int found) {
        return new SurveyResult(container, location, total, found);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurveyResult that = (SurveyResult) o;

        if (location != that.location) return false;
        if (total != that.total) return false;
        if (found != that.found) return false;
        return container.equals(that.container);

    }

    @Override
    public int hashCode() {
        int result = container.hashCode();
        result = 31 * result + location;
        result = 31 * result + total;
        result = 31 * result + found;
        return result;
    }
}
