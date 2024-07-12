package io.github.opensabe.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.List;
import java.util.Set;

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
                JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(t));
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
