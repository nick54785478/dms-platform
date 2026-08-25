/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


CREATE TABLE IF NOT EXISTS template_field_mapping (
  id BIGSERIAL NOT NULL,
  template_code VARCHAR(255) NOT NULL,
  template_sheet_name VARCHAR(255),
  header_name VARCHAR(255) NOT NULL,
  mapping_field_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO validation_policy (id, code, type, template_name, template_sheet_name, mapping_field_name, rule, expression, error_message, priority_no, active_flag) VALUES
(1, 'DEPT_PROFILE', 'ROW', 'DEPT_PROFILE', 'MIS', 'NAME', 'ENFORCE_ROW_VALIDATION', '#checkedValue!=''''', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空"', 1, 'Y');
INSERT INTO validation_policy (id, code, type, template_name, template_sheet_name, mapping_field_name, rule, expression, error_message, priority_no, active_flag) VALUES
(2, 'DEPT_PROFILE', 'ROW', 'DEPT_PROFILE', 'MIS', 'AGE', 'ENFORCE_ROW_VALIDATION', '#isNumeric(#checkedValue) && #checkedValue!='''' && #checkedValue.matches(''^[1-9]\\d*$'')', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空或需為整數"', 2, 'Y');
INSERT INTO validation_policy (id, code, type, template_name, template_sheet_name, mapping_field_name, rule, expression, error_message, priority_no, active_flag) VALUES
(3, 'DEPT_PROFILE', 'ROW', 'DEPT_PROFILE', 'MIS', 'SEX', 'ENFORCE_ROW_VALIDATION', '#checkedValue!=''''', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空"', 3, 'Y');
INSERT INTO validation_policy (id, code, type, template_name, template_sheet_name, mapping_field_name, rule, expression, error_message, priority_no, active_flag) VALUES
(4, 'DEPT_PROFILE', 'ROW', 'DEPT_PROFILE', 'MIS', 'NATION_ID', 'ENFORCE_ROW_VALIDATION', '#checkedValue!='''' &&  #checkedValue.matches(''^[A-Z][12]\\d{8}$'')', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空或身分證字號格式有誤 "', 4, 'Y'),
(5, 'DEPT_PROFILE', 'ROW', 'DEPT_PROFILE', 'HR', 'NAME', 'ENFORCE_ROW_VALIDATION', '#checkedValue!=''''', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空"', 5, 'Y'),
(6, 'DEPT_PROFILE', 'ROW', 'DEPT_PROFILE', 'HR', 'AGE', 'ENFORCE_ROW_VALIDATION', '#isNumeric(#checkedValue) && #checkedValue!='''' && #checkedValue.matches(''^[1-9]\\d*$'')', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空或需為整數"', 6, 'Y'),
(7, 'DEPT_PROFILE', 'ROW', 'DEPT_PROFILE', 'HR', 'SEX', 'ENFORCE_ROW_VALIDATION', '#checkedValue!=''''', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空"', 7, 'Y'),
(8, 'DEPT_PROFILE', 'ROW', 'DEPT_PROFILE', 'HR', 'NATION_ID', 'ENFORCE_ROW_VALIDATION', '#checkedValue!='''' &&  #checkedValue.matches(''^[A-Z][12]\\d{8}$'')', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空或身分證字號格式有誤 "', 8, 'Y'),
(9, 'USER_PROFILE', 'ROW', 'USER_PROFILE', 'USER', 'NAME', 'ENFORCE_ROW_VALIDATION', '#checkedValue!=''''', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空 "', 9, 'Y'),
(10, 'USER_PROFILE', 'ROW', 'USER_PROFILE', 'USER', 'AGE', 'ENFORCE_ROW_VALIDATION', '#isNumeric(#checkedValue) && #checkedValue!='''' && #checkedValue.matches(''^[1-9]\\d*$'')', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空或需為整數 "', 10, 'Y'),
(11, 'USER_PROFILE', 'ROW', 'USER_PROFILE', 'USER', 'SEX', 'ENFORCE_ROW_VALIDATION', '#checkedValue!=''''', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空 "', 11, 'Y'),
(12, 'USER_PROFILE', 'ROW', 'USER_PROFILE', 'USER', 'NATION_ID', 'ENFORCE_ROW_VALIDATION', '#checkedValue!='''' &&  #checkedValue.matches(''^[A-Z][12]\\d{8}$'')', '''SheetName : '' + #sheetName + '', ''  + #excelAddress + " 資料檢核有誤，不能為空或身分證字號格式有誤  "', 12, 'Y'),
(13, 'USER_PROFILE', 'SHEET', 'USER_PROFILE', 'USER', 'NATION_ID', '', '#validateDuplicate(#sheet, ''NATION_ID'')', '', 13, 'Y'),
(14, 'USER_PROFILE', 'SHEET', 'USER_PROFILE', 'DEPT', 'deptList', 'VARIABLE', '{''資訊部'',''人力資源部'',''會計部'',''行銷部''}', '', 14, 'Y'),
(15, 'USER_PROFILE', 'SHEET', 'USER_PROFILE', 'DEPT', 'DEPT', '', '#contains(#deptList, #sheet, ''DEPT'')', '', 15, 'Y');

INSERT INTO template_field_mapping (id, template_code, template_sheet_name, header_name, mapping_field_name) VALUES
(1, 'DEPT_PROFILE', 'MIS', '部門名稱', 'NAME'),
(2, 'DEPT_PROFILE', 'MIS', '年齡', 'AGE'),
(3, 'DEPT_PROFILE', 'MIS', '性別', 'SEX'),
(4, 'DEPT_PROFILE', 'MIS', '身分證字號', 'NATION_ID'),
(5, 'DEPT_PROFILE', 'HR', '部門名稱', 'NAME'),
(6, 'DEPT_PROFILE', 'HR', '年齡', 'AGE'),
(7, 'DEPT_PROFILE', 'HR', '性別', 'SEX'),
(8, 'DEPT_PROFILE', 'HR', '身分證字號', 'NATION_ID'),
(9, 'USER_PROFILE', 'USER', '用戶姓名', 'NAME'),
(10, 'USER_PROFILE', 'USER', '年齡', 'AGE'),
(11, 'USER_PROFILE', 'USER', '性別', 'SEX'),
(12, 'USER_PROFILE', 'USER', '身分證字號', 'NATION_ID'),
(13, 'USER_PROFILE', 'DEPT', '部門', 'DEPT');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
SELECT setval('template_field_mapping_id_seq', (SELECT MAX(id) FROM template_field_mapping));
SELECT setval('validation_policy_id_seq', (SELECT MAX(id) FROM validation_policy));

