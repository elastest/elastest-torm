/*
 * (C) Copyright 2017-2019 ElasTest (http://elastest.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.elastest.epm.client;

import io.elastest.epm.client.json.DockerComposeConfig;
import io.elastest.epm.client.json.DockerComposeList;
import io.elastest.epm.client.json.DockerContainerInfo;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Docker Compose UI REST service.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.1.1
 */
public interface DockerComposeApi {

    @POST("/api/v1/create-project")
    Call<ResponseBody> createProject(@Body RequestBody data);

    @POST("/api/v1/projects")
    Call<ResponseBody> dockerComposeUp(@Body RequestBody data);

    @POST("/api/v1/down")
    Call<ResponseBody> dockerComposeDown(@Body RequestBody data);

    @GET("/api/v1/projects")
    Call<DockerComposeList> listProjects();

    @GET("/api/v1/projects/yml/{projectName}")
    Call<DockerComposeConfig> getDockerComposeYml(
            @Path("projectName") String projectName);

    @DELETE("/api/v1/remove-project/{projectName}")
    Call<DockerComposeConfig> removeProject(
            @Path("projectName") String projectName);

    @GET("/api/v1/projects/{projectName}")
    Call<DockerContainerInfo> getContainers(
            @Path("projectName") String projectName);

}
