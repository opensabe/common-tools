/*
 * Copyright 2025 opensabe-tech
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
 */
package io.github.opensabe.common.s3.typehandler;

import io.github.opensabe.common.s3.properties.S3Properties;
import io.github.opensabe.common.s3.service.FileService;
import io.github.opensabe.common.typehandler.OBSService;
import io.github.opensabe.common.typehandler.OBSTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

public class S3OBSService implements OBSService {

    private final FileService s3SyncFileService;

    private final String fileName;

    public S3OBSService(FileService s3SyncFileService, S3Properties properties, String defaultOperId) {
        this.s3SyncFileService = s3SyncFileService;
        if (StringUtils.isNotBlank(properties.getFolderName())) {
            if (!properties.getFolderName().contains("/")) {
                throw new IllegalArgumentException("folderName must contain two levels of directories");
            }
            //这里的folderName必须包含二级目录
            var arr = properties.getFolderName().split("/");
            this.fileName = arr[0] + "/"+ arr[1] + "/typehandler/%s.json";
        } else {
            var country =  "default";
            this.fileName = properties.getProfile()+"/" +country + "/typehandler/%s.json";
        }
    }


    @Override
    public OBSTypeEnum type() {
        return OBSTypeEnum.S3;
    }

    @Override
    public void insert(String key, String json) {
        var name = String.format(fileName,key);
        s3SyncFileService.putObjectAssignedPath(json.getBytes(StandardCharsets.UTF_8),name, MediaType.APPLICATION_JSON_VALUE);
    }

    @Override
    public String select(String key) {
        var name = String.format(fileName,key);
        return new String(s3SyncFileService.getObject(name));
    }

}
