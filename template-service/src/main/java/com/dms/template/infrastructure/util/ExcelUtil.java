package com.dms.template.infrastructure.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.util.ReflectionUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 處理 Excel 匯出與匯入的基礎設施工具類別 (Infrastructure Utility).
 *
 * <p>
 * 提供將資料匯出成 Excel 位元組陣列或 InputStreamResource 的功能，
 * 也提供讀取 Excel 檔案並轉為 Map 資料結構的功能。
 * 此類別將底層 Apache POI 的操作封裝起來，避免讓核心領域或應用層
 * 直接依賴基礎設施的細節。
 * </p>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelUtil {

    // ### 對外 API ###

    /**
     * 讀取 Excel 並解析所有工作表 (Sheet).
     *
     * @param inputStream Excel 檔案的 InputStream
     * @return 包含所有 Sheet 資料的 Map，Key 為 Sheet 名稱，Value 為每列資料的 List (以欄位名稱為 Key 的 Map)
     * @throws IOException 當讀取 Excel 串流發生錯誤時拋出
     */
    public static Map<String, List<Map<String, String>>> readExcelData(InputStream inputStream) throws IOException {
        Map<String, List<Map<String, String>>> result = new HashMap<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                result.put(sheetName, parseSheet(sheet));
            }
        }
        return result;
    }

    /**
     * 讀取指定 Sheet 名稱的 Excel 資料.
     *
     * @param inputStream Excel 檔案的 InputStream
     * @param sheetName   要讀取的指定工作表名稱
     * @return 該 Sheet 的資料列集合，若找不到該 Sheet 則回傳空 List
     * @throws IOException 當讀取 Excel 串流發生錯誤時拋出
     */
    public static List<Map<String, String>> readExcelData(InputStream inputStream, String sheetName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            return sheet == null ? List.of() : parseSheet(sheet);
        }
    }

    /**
     * 讀取指定多個 Sheet 名稱的 Excel 資料.
     *
     * @param inputStream   Excel 檔案的 InputStream
     * @param sheetNameList 預計要讀取的 Sheet 名稱列表
     * @return 包含指定 Sheet 資料的 Map
     * @throws IOException 當讀取 Excel 串流發生錯誤時拋出
     */
    public static Map<String, List<Map<String, String>>> readExcelData(InputStream inputStream, List<String> sheetNameList) throws IOException {
        Map<String, List<Map<String, String>>> result = new HashMap<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            for (String sheetName : sheetNameList) {
                Sheet sheet = workbook.getSheet(sheetName);
                result.put(sheetName, sheet == null ? List.of() : parseSheet(sheet));
            }
        }
        return result;
    }

    /**
     * 解析單一 Sheet 的內容，將其轉換為以表頭名稱為鍵值的 Map 集合.
     *
     * <p>
     * 處理邏輯：
     * 1. 讀取第一列作為資料表頭 (Header)
     * 2. 從第二列開始逐列讀取資料
     * 3. 將每列資料的每一個儲存格 (Cell) 對應至對應的表頭，組裝成 Map
     * 4. 自動過濾掉全為空值的空白列
     * </p>
     *
     * @param sheet 準備被解析的工作表 (Sheet) 物件
     * @return 該工作表轉換後的資料列集合，每列資料為一個 {@code Map<String, String>}
     */
    private static List<Map<String, String>> parseSheet(Sheet sheet) {
        List<Map<String, String>> data = new ArrayList<>();
        if (sheet == null) return data;

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return data;

        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(StringUtils.trim(parseCellValue(cell)));
        }

        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            Map<String, String> rowData = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String value = StringUtils.trim(parseCellValue(cell));
                rowData.put(headers.get(c), value);
            }

            if (isRowEmpty(rowData)) continue;
            data.add(rowData);
        }
        return data;
    }

    /**
     * 判斷給定的資料列是否全為空值 (空白列防禦).
     *
     * @param rowData 單列資料的 Map，Key 為表頭，Value 為該欄的值
     * @return 如果該列所有的值都是 null、空字串或僅含空白字元，則回傳 true
     */
    private static boolean isRowEmpty(Map<String, String> rowData) {
        return rowData.values().stream().allMatch(StringUtils::isBlank);
    }

    /**
     * 依據不同的資料型別解析單一儲存格 (Cell) 的值.
     *
     * <p>
     * 支援的 CellType 包括：
     * <ul>
     * <li><b>STRING:</b> 直接回傳字串</li>
     * <li><b>NUMERIC:</b> 轉為字串回傳</li>
     * <li><b>BOOLEAN:</b> 轉為字串 ("true"/"false") 回傳</li>
     * <li><b>FORMULA:</b> 嘗試取得計算後的數值，若失敗則退回嘗試取得字串</li>
     * </ul>
     * 其他型別或遇到 null 一律回傳空字串 ("")。
     * </p>
     *
     * @param cell 欲解析的 Excel 儲存格
     * @return 解析後的儲存格字串值
     */
    private static String parseCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    yield cell.getStringCellValue();
                }
            }
            default -> "";
        };
    }

    // ### Export Excel ###

    /**
     * 將資料匯出為 InputStreamResource，方便 Spring ResponseEntity 使用.
     *
     * <p>
     * <b>注意：</b> 此方法會使用 Reflection 讀取 rowDataSet 中物件的欄位。
     * 若資料為 Map 或是陣列，請改用 {@link #exportDataAsByteArrayFromArrays}。
     * </p>
     *
     * @param sheetName  工作表 (Sheet) 名稱
     * @param headerList 表頭列表
     * @param rowDataSet 資料列集合 (必須為 POJO 物件集合)
     * @return Spring 用的 InputStreamResource 封裝，若發生錯誤則回傳 null
     */
    public static InputStreamResource exportDataAsResource(String sheetName, List<String> headerList, List<?> rowDataSet) {
        try (XSSFWorkbook book = processWorkbook(sheetName, headerList, rowDataSet);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            book.write(bos);
            return new InputStreamResource(new ByteArrayInputStream(bos.toByteArray()));
        } catch (IOException e) {
            log.error("轉換錯誤，產生報表失敗", e);
            return null;
        }
    }

    /**
     * 將資料匯出為 byte[] 位元組陣列.
     *
     * <p>
     * <b>注意：</b> 此方法會使用 Reflection 讀取 rowDataSet 中物件的欄位。
     * 若資料為 Map 或是陣列，請改用 {@link #exportDataAsByteArrayFromArrays}。
     * </p>
     *
     * @param sheetName  工作表 (Sheet) 名稱
     * @param headerList 表頭列表
     * @param rowDataSet 資料列集合 (必須為 POJO 物件集合)
     * @return 產生的 Excel 檔案二進位資料 (byte[])，若發生錯誤則回傳空陣列
     */
    public static byte[] exportDataAsByteArray(String sheetName, List<String> headerList, List<?> rowDataSet) {
        try (XSSFWorkbook book = processWorkbook(sheetName, headerList, rowDataSet);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            book.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("轉換錯誤，產生報表失敗", e);
            return new byte[0];
        }
    }

    /**
     * 將指定的表頭與二維陣列資料匯出為 Excel 位元組陣列 (byte[]).
     *
     * <p>
     * 提供直接接收 {@code List<Object[]>} 的多載方法，以避開原本 {@link #exportDataAsByteArray} 
     * 針對 POJO 的 Reflection 機制。適用於動態產生欄位的場景 (例如：JSON 定義的動態表單)。
     * </p>
     *
     * @param sheetName  Excel 內的工作表 (Sheet) 名稱
     * @param headerList 表頭列表 (如: ["姓名", "年齡"])
     * @param rowDataSet 資料列集合，每個 Object[] 代表一列資料
     * @return 產生的 Excel 檔案二進位資料 (byte[])，可直接用於 HTTP Response
     */
    public static byte[] exportDataAsByteArrayFromArrays(String sheetName, List<String> headerList, List<Object[]> rowDataSet) {
        try (XSSFWorkbook book = processWorkbookFromArrays(sheetName, headerList, rowDataSet);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            book.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("轉換錯誤，產生報表失敗", e);
            return new byte[0];
        }
    }

    /**
     * 建立並處理 Excel 的 XSSFWorkbook (POJO 反射版).
     *
     * @param sheetName  工作表名稱
     * @param headerList 表頭列表
     * @param rowDataSet POJO 資料列集合
     * @return 已經填滿表頭與資料的 {@link XSSFWorkbook} 實體
     */
    public static XSSFWorkbook processWorkbook(String sheetName, List<String> headerList, List<?> rowDataSet) {
        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet sheet = book.createSheet(sheetName);
        Object[] headers = headerList.toArray();
        List<Object[]> dataset = new ArrayList<>();
        rowDataSet.forEach(e -> dataset.add(convertObjectToArray(e)));
        importData(sheet, headers, dataset);
        return book;
    }

    /**
     * 建立並處理 Excel 的 XSSFWorkbook (直接接收陣列資料版).
     *
     * @param sheetName  Excel 內的工作表 (Sheet) 名稱
     * @param headerList 表頭列表
     * @param rowDataSet 資料列集合，每個 Object[] 代表一列資料
     * @return 已經填滿表頭與資料的 {@link XSSFWorkbook} 實體
     */
    public static XSSFWorkbook processWorkbookFromArrays(String sheetName, List<String> headerList, List<Object[]> rowDataSet) {
        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet sheet = book.createSheet(sheetName);
        Object[] headers = headerList.toArray();
        importData(sheet, headers, new ArrayList<>(rowDataSet)); // 用新 ArrayList 避免修改原 list 結構(importData會插header)
        return book;
    }

    /**
     * 負責將表頭與資料列實際寫入至給定的 Excel 工作表中.
     *
     * @param sheet      要寫入的 XSSFSheet 工作表實體
     * @param header     表頭資料 (Object[])，若為 null 則略過寫入表頭
     * @param rowDataSet 準備寫入的二維資料列集合 (List<Object[]>)
     */
    public static void importData(XSSFSheet sheet, Object[] header, List<Object[]> rowDataSet) {
        int rowIdx = -1;
        if (header != null) {
            rowDataSet.add(0, header);
        } else {
            rowIdx = 0;
        }

        for (Object[] arrs : rowDataSet) {
            XSSFRow row = sheet.createRow(++rowIdx);
            int colIdx = -1;
            
            if (arrs == null) continue;
            
            for (Object field : arrs) {
                XSSFCell cell = row.createCell(++colIdx);
                if (field == null) {
                    cell.setCellValue("");
                    continue;
                }

                switch (field.getClass().getSimpleName()) {
                    case "String":
                        cell.setCellValue((String) field);
                        break;
                    case "Integer":
                        cell.setCellValue((Integer) field);
                        break;
                    case "Long":
                        cell.setCellValue((Long) field);
                        break;
                    case "Double":
                        cell.setCellValue((Double) field);
                        break;
                    case "Date":
                        cell.setCellValue(DateFormatUtils.format((Date) field, "yyyy/MM/dd"));
                        break;
                    case "BigDecimal":
                        cell.setCellValue(((BigDecimal) field).doubleValue());
                        break;
                    default:
                        cell.setCellValue(field.toString());
                }
            }
        }
    }

    /**
     * 利用 Reflection (反射) 將單一 POJO 物件轉換為 Object[] 陣列.
     *
     * @param obj 任意 POJO 物件
     * @return 取出所有欄位值後的 Object[] 陣列
     */
    private static Object[] convertObjectToArray(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        Object[] objectArray = new Object[fields.length];
        try {
            for (int i = 0; i < fields.length; i++) {
                ReflectionUtils.makeAccessible(fields[i]);
                objectArray[i] = fields[i].get(obj);
            }
        } catch (IllegalAccessException e) {
            log.error("物件轉換發生非預期的錯誤");
        }
        return objectArray;
    }
}
