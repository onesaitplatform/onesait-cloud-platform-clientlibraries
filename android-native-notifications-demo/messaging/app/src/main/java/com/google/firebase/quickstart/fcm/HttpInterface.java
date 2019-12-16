/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.quickstart.fcm;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface HttpInterface {

    @Headers({"X-OP-APIKey: 2add806440f44a0e87591e7ca1049b1b", "Content-Type: application/json", "Accept: application/json"})
    @POST("v1/registerNotificationClientToken/")
    Call<PostResponse> postJson(@Body NativeNotifKeysOntology instance);

    @Headers({"X-OP-APIKey: 2add806440f44a0e87591e7ca1049b1b", "Content-Type: application/json", "Accept: application/json"})
    @PUT("v1/registerNotificationClientToken/{id}")
    Call<ResponseBody> putJson(@Path("id") String onesaitId, @Body NativeNotifKeysOntology instance);
}
