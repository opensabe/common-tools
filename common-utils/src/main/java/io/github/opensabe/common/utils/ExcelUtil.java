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
package io.github.opensabe.common.utils;

import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.alibaba.fastjson.JSONObject;

import io.github.opensabe.common.utils.json.JsonUtil;

public class ExcelUtil {
    public static <T> byte[] transfer(List<T> objects) {
        HSSFWorkbook wb = null;
        try {
            wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("result");
            int rowNo = 0;
            int colNo = 0;
            Set<String> keys = null;
            for (T t : objects) {
                JSONObject jsonObject = JSONObject.parseObject(JsonUtil.toJSONString(t));
                HSSFRow row = sheet.createRow(rowNo++);
                // 创建HSSFCell对象
                if (keys == null) {
                    //标题
                    keys = jsonObject.keySet();
                    for (String s : keys) {
                        HSSFCell cell = row.createCell(colNo++);
                        cell.setCellValue(s);
                    }
                    colNo = 0;
                    row = sheet.createRow(rowNo++);
                }
                for (String s : keys) {
                    HSSFCell cell = row.createCell(colNo++);
                    cell.setCellValue(jsonObject.getString(s));
                }
                colNo = 0;
            }
            return wb.getBytes();
        } finally {
            try {
                wb.close();
            } catch (Exception e) {

            }
        }
    }
}
