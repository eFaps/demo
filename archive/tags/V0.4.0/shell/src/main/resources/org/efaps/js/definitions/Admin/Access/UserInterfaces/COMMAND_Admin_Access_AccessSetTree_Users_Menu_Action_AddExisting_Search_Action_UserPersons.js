/*
 * Copyright 2006 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:          tmo
 * Revision:        $Rev:356 $
 * Last Changed:    $Date:2006-09-06 03:06:13 +0200 (Mi, 06 Sep 2006) $
 * Last Changed By: $Author:tmo $
 */

with (COMMAND)  {
  addProperty("ConnectChildAttribute",  "UserAbstractLink");
  addProperty("ConnectParentAttribute", "AccessSetLink");
  addProperty("ConnectType",            "Admin_Access_AccessSet2UserAbstract");
  addProperty("ResultTable",            "Admin_User_PersonTable");
  addProperty("SearchForm",             "Admin_User_PersonForm");
  addProperty("SearchType",             "Admin_User_Person");
  addProperty("TargetMode",             "connect");
}

