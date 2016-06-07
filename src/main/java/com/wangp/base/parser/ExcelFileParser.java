package com.wangp.base.parser;

import com.wangp.common.BeanUtils;
import com.wangp.common.Logger;
import com.wangp.base.parser.exception.FileParseException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangpeng627 on 16-3-10.
 */
public class ExcelFileParser<T> extends AbstructFileParser<T> {

    private Class clazz;

    private void SetClazz(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public List<T> parse(String fileName, Class<?> clazz, LinkedHashMap<String, Class> mapRelation) throws Exception {
        SetClazz(clazz);
        List<T> list = new ArrayList<T>();
        File file = new File(fileName);
        InputStream in = new FileInputStream(file);
        Sheet sheet = null;
        if (fileName.endsWith(".xls")) {
            sheet = new HSSFWorkbook(in).getSheetAt(0);
        } else {
            sheet = new XSSFWorkbook(in).getSheetAt(0);
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            list.add(parseLine(sheet.getRow(i), i, mapRelation));
        }
        return list;
    }

    private T parseLine(Row row, int lineNo, LinkedHashMap<String, Class> mapRelation) throws Exception {
        T t = null;
        t = (T) clazz.newInstance();
        int colNum = 0;
        for (Map.Entry<String, Class> entry : mapRelation.entrySet()) {
            try {
                String key = entry.getKey();
                Class clazz = entry.getValue();
                String value = getCellString(row.getCell(colNum++));
                Object object = BeanUtils.build(value, clazz);
                PropertyUtils.setSimpleProperty(t, key, object);
            } catch (Exception e) {
                FileParseException fileParseException = new FileParseException(String.format("第%s行第%s列数据为空或者格式错误", lineNo, colNum), e);
                Logger.error(this, fileParseException.getMessage(), fileParseException.getCause());
                throw fileParseException;
            }
        }

        return t;
    }


    /**
     * 获得Excel-Sheet中单元格的字符串表示
     *
     * @param cell 单元格对象
     * @return 单元格字符串表示
     */
    protected String getCellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        int type = cell.getCellType();
        String ret = null;
        switch (type) {
            case Cell.CELL_TYPE_NUMERIC:
                ret = String.valueOf(cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_BLANK:
                ret = "";
                break;
            case Cell.CELL_TYPE_STRING:
                ret = cell.getStringCellValue();
                break;
        }
        return ret.trim();
    }

}
