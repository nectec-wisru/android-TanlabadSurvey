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

package th.or.nectec.tanrabad.entity.lookup;

public class ContainerLocation {

    public final int id;
    public final String name;

    public ContainerLocation(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ContainerLocation)) return false;
        ContainerLocation that = (ContainerLocation) other;
        return id == that.id && name.equals(that.name);
    }

    @Override
    public String toString() {
        return "ContainerLocation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
