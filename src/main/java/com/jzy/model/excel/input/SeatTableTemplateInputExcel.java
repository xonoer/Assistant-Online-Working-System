package com.jzy.model.excel.input;

import com.jzy.manager.exception.ExcelSheetNameInvalidException;
import com.jzy.manager.exception.InvalidFileTypeException;
import com.jzy.model.entity.CampusAndClassroom;
import com.jzy.model.excel.Excel;
import com.jzy.model.excel.ExcelVersionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SeatTableTemplateInputExcel
 * @Author JinZhiyun
 * @Description 导入的座位表模板的模型类
 * @Date 2019/11/28 11:34
 * @Version 1.0
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class SeatTableTemplateInputExcel extends Excel implements Serializable {
    private static final long serialVersionUID = -4498593702973804852L;

    public SeatTableTemplateInputExcel() {
    }

    public SeatTableTemplateInputExcel(String inputFile) throws IOException, InvalidFileTypeException {
        super(inputFile);
    }

    public SeatTableTemplateInputExcel(File file) throws IOException, InvalidFileTypeException {
        super(file);
    }

    public SeatTableTemplateInputExcel(InputStream inputStream, ExcelVersionEnum version) throws IOException, InvalidFileTypeException {
        super(inputStream, version);
    }

    public SeatTableTemplateInputExcel(Workbook workbook) {
        super(workbook);
    }

    /**
     * 从座位表模板中读取的教室
     */
    private List<CampusAndClassroom> campusAndClassrooms;


    /**
     * 根据输入输入的座位表读取教室信息
     *
     * @return 所有校区和教室对象的封装集合
     * @throws IOException 处理excel时的io异常
     * @throws ExcelSheetNameInvalidException 不合法的sheet名，这里指教室门牌号不是纯数字
     */
    public List<CampusAndClassroom> readSeatTable() throws IOException, ExcelSheetNameInvalidException {
        resetParam();

        int sheetCount = this.getSheetCount();
        for (int i = 0; i < sheetCount; i++) {
            String classroom = this.getSheetName(i);
            if (!StringUtils.isNumeric(classroom)) {
                //如果sheet名（教室门牌号）不是纯数字
                throw new ExcelSheetNameInvalidException("教室门牌号不是纯数字!");
            }
            CampusAndClassroom campusAndClassroom = new CampusAndClassroom();
            campusAndClassroom.setClassroom(classroom);

            int rowCount = this.getRowCount(i);
            Integer maxCapacity = null;
            for (int j = 0; j < rowCount; j++) {
                for (int k = 0; k < this.getColumnCount(i, j); k++) {
                    //遍历表格所有行
                    String value = this.getValueAt(i, j, k);
                    if (StringUtils.isNumeric(value)) {
                        //对所有为数字的单元格找到最大的作为当前教室容量
                        Integer cap = Integer.parseInt(value);
                        if (maxCapacity == null || cap > maxCapacity) {
                            maxCapacity = cap;
                        }
                    }
                }
            }
            campusAndClassroom.setClassroomCapacity(maxCapacity);

            campusAndClassrooms.add(campusAndClassroom);
        }

        return campusAndClassrooms;
    }

    @Override
    public void resetParam() {
        campusAndClassrooms = new ArrayList<>();
    }
}

