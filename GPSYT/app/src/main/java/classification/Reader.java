package classification;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Reader {
    Workbook workbook;
    Sheet sheet;

    public Reader(InputStream inputData) throws IOException, InvalidFormatException {
        workbook = WorkbookFactory.create(inputData);
        sheet = workbook.getSheetAt(0);
    }

    public void getData(ArrayList<Activity> activities) {

        for (Row row : sheet) {
            String activityName = "";
            ArrayList<Double> values = new ArrayList<>();
            if (row.getRowNum() != 0) {
                for (Cell cell : row) {
                    if (cell.getCellTypeEnum() == CellType.STRING) {
                        activityName = cell.getStringCellValue();
                    }
                    else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                            values.add(cell.getNumericCellValue());
                    }
                }
                activities.add(new Activity(activityName, values));
            }
        }
    }
}