package utils;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ReadXLS_X {

    public static Map<String,String> read(int columnIndex){
        ArrayList<String> columndata = null;
        Map<String,String> data = new HashMap<>();

        try {
            File f = new File("/home/zealot/IdeaProjects/spark_twitter_streaming/src/main/resources/tweets_with_sentiment.xlsx");
            OPCPackage opcPackage = OPCPackage.open(f);
            XSSFWorkbook workbook = new XSSFWorkbook(opcPackage);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            columndata = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    Cell cell2 = cellIterator.next();

                    if(row.getRowNum() > 0){ //To filter column headings
                        if(cell.getColumnIndex() == columnIndex){// To match column index
                            switch (cell.getCellType()) {
                                case Cell.CELL_TYPE_NUMERIC:
                                    columndata.add(cell.getNumericCellValue()+"");
                                    break;
                                case Cell.CELL_TYPE_STRING:
                                    data.put(cell.getStringCellValue(),cell2.getStringCellValue());
                                    columndata.add(cell.getStringCellValue());
                                    break;
                            }
                        }
                    }
                }
            }
            System.out.println(columndata);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void save(Map<String, String> data) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter("/home/zealot/IdeaProjects/spark_twitter_streaming/src/main/resources/tweets_with_sentiment2.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterator<Map.Entry<String,String>> iterator = data.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,String> pairs = iterator.next();
            try {
                out.write(pairs.getValue() + "   " + pairs.getKey() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        out.close();
    }

    public static void main(String[] args) {
        try {
            save(read(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
